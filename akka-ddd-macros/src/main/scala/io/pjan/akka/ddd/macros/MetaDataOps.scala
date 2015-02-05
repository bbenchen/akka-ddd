package io.pjan.akka.ddd.macros

import scala.language.experimental.macros


object MetaDataOps {
  import scala.reflect.macros.whitebox.Context

  def withMetaData[T, M](entity: T, metaData: M): T = macro withMetaDataImpl[T, M]

  def withMetaDataImpl[T: c.WeakTypeTag, M: c.WeakTypeTag](c: Context)(entity: c.Expr[T], metaData: c.Expr[M]): c.Expr[T] = {
    import c.universe._

    val tree = reify(entity.splice).tree
    val copy = entity.actualType.member(TermName("copy"))

    c.Expr[T](Apply(
      Select(tree, copy),
      AssignOrNamedArg(Ident(TermName("metaData")), reify(metaData.splice).tree) :: Nil
    ))
  }
}