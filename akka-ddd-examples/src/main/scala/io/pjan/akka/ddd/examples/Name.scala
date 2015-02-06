package io.pjan.akka.ddd.examples


case class Name(
    firstName: String,
    lastName: String,
    middleName: Option[String] = None,
    prefix: Option[String] = None,
    suffix: Option[String] = None) {
  require(firstName.length > 0, "First name must be at least one character in length.")
  require(firstName.length < 50, "First name must be 50 characters or less.")
  require(lastName.length > 0, "Last name must be at least one character in length.")
  require(lastName.length < 50, "Last name must be 50 characters or less.")

  def withChangedFirstName(firstName: String): Name = copy(firstName = firstName)

  def withChangedLastName(lastName: String): Name = copy(lastName = lastName)

  def withMiddleName(middleName: String): Name = copy(middleName = Some(middleName))

  def withPrefix(prefix: String): Name = copy(prefix = Some(prefix))

  def withSuffix(suffix: String): Name = copy(suffix = Some(suffix))
}

