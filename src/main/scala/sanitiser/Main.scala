package sanitiser

import java.util.Properties
import edu.stanford.nlp.pipeline.{CoreDocument, StanfordCoreNLP}
import scala.io.Source
import scala.collection.JavaConverters._
import java.io.PrintWriter
import scala.io.StdIn

object Main extends App {
  val props = new Properties
  props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner")

  // customize fine grained ner
  props.setProperty("ner.fine.regexner.ignorecase", "true")

  // customise additional ner
  props.setProperty("ner.additional.regexner.mapping", "additional.rules")
  props.setProperty("ner.additional.regexner.ignorecase", "true")
  props.setProperty("ner.additional.regexner.backgroundSymbol", "PERSON,NUMBER,MISC,O")

  val pipeline = new StanfordCoreNLP(props)

  val inputFile = StdIn.readLine("Filename to sanitise: ")

  val rawDoc = Source.fromFile(s"Files/${inputFile}").getLines.mkString("\n")

  val doc = new CoreDocument(rawDoc)
  pipeline.annotate(doc)

  val piiEntities = List("PERSON", "ADDRESS", "CODE", "ORGANIZATION", "EMAIL", "NUMBER", "LOCATION", "COUNTRY", "URL", "MONEY")

  val entityList = doc
    .entityMentions()
    .asScala
    .toSet

  // filter out text that NER has identified as 'PERSON' but is not personal, e.g. 'him' or 'her'
  val discoveredNotPii = entityList
    .filter(_.entityType == "NOTPII")
    .map(_.text)

  // filter out text that NER has identified as 'NUMBER' but is not a digit, such as 'one' or 'two'
  val notNumRegex = "(\\D+)"
  val discoveredNotNum = entityList
    .filter(_.entityType == "NUMBER")
    .filter(_.text matches notNumRegex)
    .map(_.text)

  // filter out references to theguardian.com in URLs and emails
  val theGuardian = "guardian"
  val discoveredGuardian = entityList
    .filter(_.text.toLowerCase contains theGuardian)
    .map(_.text)

  // filter out text that has been identified as a sentence but not as an entity of any kind that contains
  // a mixture of numbers, letters and symbols (password)
  val sentenceList = doc
    .sentences()
    .asScala
    .toSet
  val passwordRegex = "(\\d+[\\x21-\\x7E]+|[\\x21-\\x7E]+\\d+)"
  val passwordList = sentenceList
    .filter(_.text matches passwordRegex)
    .map(_.text)

  // create 'master' list of PII to remove from the file
  val piiList = (entityList
    .filter(entityMention => piiEntities.contains(entityMention.entityType))
    .map(_.text)
    .flatMap(_.split("(\\s)"))
    .diff(discoveredNotPii)
    .diff(discoveredNotNum)
    .diff(discoveredGuardian) ++ passwordList)
    .flatMap(_.split("""\u00A0"""))

  def wordContainsPii(word: String): Option[String] = {
    piiList.find(pii => word.contains(pii))
  }

  val sanitisedDoc = rawDoc
    .split("""\n""")
    .flatMap(_.split(" "))
    .map ({
      word => wordContainsPii(word) match {
        case Some(pii) => word.replaceAll(pii, "")
        case None => word
      }
    })
    .mkString(" ")

  new PrintWriter("Files/sanitised.txt") { write(sanitisedDoc.mkString("")); close() }
  new PrintWriter("Files/pii.txt") { write(piiList.mkString("\n")); close() }

  println("File processed. Please view sanitised.txt to view sanitised text. PII removed can be found in pii.txt.")

}