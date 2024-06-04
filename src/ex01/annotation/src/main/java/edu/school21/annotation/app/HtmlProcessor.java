package edu.school21.annotation.app;

import com.google.auto.service.AutoService;
import edu.school21.annotation.annotations.HtmlForm;
import edu.school21.annotation.annotations.HtmlInput;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SupportedAnnotationTypes({"edu.school21.annotation.annotations.HtmlForm",
        "edu.school21.annotation.annotations.HtmlInput"})
@AutoService(Processor.class)
public class HtmlProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(HtmlForm.class);
        for (Element element : annotatedElements) {
            generateHtml(element);
        }
        return true;
    }

    private void generateHtml(Element element) {
        String fileName = element.getAnnotation(HtmlForm.class).fileName();
        String action = element.getAnnotation(HtmlForm.class).action();
        String method = element.getAnnotation(HtmlForm.class).method();
        try (PrintWriter out = new PrintWriter(processingEnv.getFiler()
                .createResource(StandardLocation.CLASS_OUTPUT, "", fileName).openWriter())) {
            out.println("<form action = \"" + action + "\" method = \"" + method + "\">");
            List<? extends Element> l =  element.getEnclosedElements().stream().filter(e -> e.getKind().isField())
                    .filter(e -> e.getAnnotation(HtmlInput.class) != null).collect(Collectors.toList());
            for (Element e : l) {
                String type = e.getAnnotation(HtmlInput.class).type();
                String name = e.getAnnotation(HtmlInput.class).name();
                String placeholder = e.getAnnotation(HtmlInput.class).placeholder();
                out.println("\t<input type = \""
                        + type + "\" name = \""
                        +name + "\" placeholder = \""
                        + placeholder+ "\">");
            }
            out.println("\t<input type = \"submit\" value = \"Send\">");
            out.println("</form>");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
