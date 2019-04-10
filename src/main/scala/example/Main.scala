package example

import java.util.Properties
import edu.stanford.nlp.pipeline.{CoreDocument, StanfordCoreNLP}
import scala.io.Source
import scala.collection.JavaConverters._

object Main extends App {
  val props = new Properties
  props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner")
  // example customizations (these are commented out but you can uncomment them to see the results
  // customize fine grained ner
  // props.setProperty("ner.fine.regexner.mapping", "example.rules");
  props.setProperty("ner.fine.regexner.ignorecase", "true")
  //  props.setProperty("ner.additional.regexner.mapping", "ignorecase=true,example_one.rules;example_two.rules")
  props.setProperty("ner.additional.regexner.mapping", "sanitizer.rules")
  props.setProperty("ner.additional.regexner.ignorecase", "true")
  val pipeline = new StanfordCoreNLP(props)
  val lines = Source.fromFile("file.txt").getLines

  lines.foreach { rawLine =>
    val doc = new CoreDocument(rawLine)
    pipeline.annotate(doc)
    val tokenizedRawLine = doc.tokens.asScala.toList.map(_.word)
    doc.entityMentions().asScala.toList.foreach(em => println(s"${em.text} : ${em.entityType()}"))
    val pii = doc.entityMentions().asScala.toList.map(_.text)

    println("Sanitised:")
    println(tokenizedRawLine.diff(pii))
  }
}

