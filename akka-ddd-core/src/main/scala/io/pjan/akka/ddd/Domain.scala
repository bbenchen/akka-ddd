package io.pjan.akka.ddd

import akka.actor._
import akka.event.Logging
import com.typesafe.config.Config
import io.pjan.akka.ddd.command._
import io.pjan.akka.ddd.identifier._
import io.pjan.akka.ddd.message._
import io.pjan.akka.ddd.providers.AggregateManagerProvider
import io.pjan.akka.ddd.support.Passivation.PassivationConfig

import scala.reflect.ClassTag

import scala.concurrent.duration._


object Domain extends ExtensionId[Domain] with ExtensionIdProvider {

  override def get(system: ActorSystem): Domain = super.get(system)

  override def lookup() = Domain

  override def createExtension(system: ExtendedActorSystem): Domain = new Domain(system)

  type IdResolver = PartialFunction[Any, Id]

  type ShardResolver = Any => String

  class Settings(val config: Config) {
    val AggregateManagerProvider: String = config.getString("am-provider")
    val AggregatesConfig: Config         = config.getConfig("aggregates")

    def getAggregateConfig(aggregateName: String): Config =
      if (AggregatesConfig.hasPath(aggregateName)) AggregatesConfig.getConfig(aggregateName).withFallback(AggregatesConfig.getConfig("default"))
      else AggregatesConfig.getConfig("default")
  }

  def commandIdResolver: IdResolver = {
    case c: Command[_] => c.aggregateId
  }

  def entityMessageIdResolver: IdResolver = {
    case em: EntityMessage[_, _] => em.entityId
  }

  def aggregateIdResolver: IdResolver = {
    commandIdResolver.orElse(entityMessageIdResolver)
  }

  // TODO: define a better default aggregateShardResolver
  def aggregateShardResolver: ShardResolver = (a: Any) => "test"

}

class Domain(val system: ExtendedActorSystem) extends Extension {
  import io.pjan.akka.ddd.Domain._

  val settings = new Settings(system.settings.config.getConfig("akka.contrib.ddd"))
  val log = Logging(system, getClass.getName)

  def aggregateManager(
    aggregateName: String,
    aggregateProps: Props,
    idResolver: Option[IdResolver] = None,
    shardResolver: Option[ShardResolver] = None): ActorRef = {
      val _idResolver = idResolver.getOrElse(aggregateIdResolver)
      aggregateManagerProvider.getAMRef(system, aggregateName, aggregateProps, _idResolver, shardResolver)
  }

  def aggregateManager[T: ClassTag](
    idResolver: IdResolver,
    shardResolver: ShardResolver)(propsArgs: Any*): ActorRef = {
      val aggregateClass              = implicitly[ClassTag[T]].runtimeClass
      val aggregateName               = aggregateClass.getSimpleName.toLowerCase
      val aggregateConfig             = settings.getAggregateConfig(aggregateName)
      val aggregatePassivationTimeout = aggregateConfig.getDuration("passivation-timeout", MILLISECONDS).millis
      val aggregatePropsArgs          = PassivationConfig(aggregateManagerProvider.passivationMessage, aggregatePassivationTimeout) +: propsArgs
      val aggregateProps              = Props(aggregateClass, aggregatePropsArgs: _*)
      aggregateManager(aggregateName, aggregateProps, Some(idResolver), Some(shardResolver))
  }

  def aggregateManagerOf[T: ClassTag](propsArgs: Any*): ActorRef =
    aggregateManager[T](aggregateIdResolver, aggregateShardResolver)(propsArgs: _*)

  def aggregateManagerOf[T: ClassTag]: ActorRef = aggregateManagerOf[T]()

  private val aggregateManagerProviderClass: String = settings.AggregateManagerProvider

  private def classLoader: ClassLoader = getClass.getClassLoader

  private def createDynamicAccess() = new ReflectiveDynamicAccess(classLoader)

  private val _dynamicAccess: DynamicAccess = createDynamicAccess()

  private def dynamicAccess: DynamicAccess = _dynamicAccess

  private val amProviderArgs = Vector(
    classOf[akka.actor.ExtendedActorSystem] -> system
  )

  private val aggregateManagerProvider: AggregateManagerProvider =
    dynamicAccess.createInstanceFor[AggregateManagerProvider](aggregateManagerProviderClass, amProviderArgs).get
}
