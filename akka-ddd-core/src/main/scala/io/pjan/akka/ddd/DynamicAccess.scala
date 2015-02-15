package io.pjan.akka.ddd

import scala.reflect.ClassTag
import scala.util.Try

// Borrowed from the Akka codebase
// https://github.com/akka/akka/blob/master/akka-actor/src/main/scala/akka/actor/DynamicAccess.scala
private[ddd] abstract class DynamicAccess {
  def createInstanceFor[T: ClassTag](cls: Class[_], args: Seq[(Class[_], AnyRef)]): Try[T]
  def getClassFor[T: ClassTag](fqcn: String): Try[Class[_ <: T]]
  def createInstanceFor[T: ClassTag](fqcn: String, args: Seq[(Class[_], AnyRef)]): Try[T]

  def classLoader: ClassLoader
}

private[ddd] class ReflectiveDynamicAccess(val classLoader: ClassLoader) extends DynamicAccess {

  def getClassFor[T: ClassTag](fqcn: String): Try[Class[_ <: T]] =
    Try[Class[_ <: T]]({
      val c = Class.forName(fqcn, false, classLoader).asInstanceOf[Class[_ <: T]]
      val t = implicitly[ClassTag[T]].runtimeClass
      if (t.isAssignableFrom(c)) c else throw new ClassCastException(s"$t is not assignable from $c")
    })

  def createInstanceFor[T: ClassTag](cls: Class[_], args: Seq[(Class[_], AnyRef)]): Try[T] =
    Try[T] {
      val types = args.map(_._1).toArray
      val values = args.map(_._2).toArray
      val constructor = cls.getDeclaredConstructor(types: _*)
      constructor.setAccessible(true)
      val obj = constructor.newInstance(values: _*)
      val t = implicitly[ClassTag[T]].runtimeClass
      if (t.isInstance(obj)) obj.asInstanceOf[T] else throw new ClassCastException(s"${cls.getName} is not a subtype of $t")
    }

  def createInstanceFor[T: ClassTag](fqcn: String, args: Seq[(Class[_], AnyRef)]): Try[T] =
    getClassFor(fqcn).flatMap { c ⇒ createInstanceFor(c, args) }

}
