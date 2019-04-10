package example

import java.util.Properties
import edu.stanford.nlp.pipeline.{CoreDocument, StanfordCoreNLP}
import scala.io.Source

object Hello extends App {
  val lines = Source.fromFile("file.txt").getLines

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

  val doc = new CoreDocument("mario@gu.com heloo world")
  pipeline.annotate(doc)
  doc.entityMentions().forEach(em => println(s"${em.text} : ${em.entityType()}"))
}

