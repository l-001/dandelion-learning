package org.dandelion.doc.swagger.config.plugin;

import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import springfox.documentation.schema.Model;
import springfox.documentation.schema.ModelProperty;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.schema.ModelBuilderPlugin;
import springfox.documentation.spi.schema.contexts.ModelContext;

import java.lang.reflect.Field;

/**
 * TODO
 *
 * @author L
 * @version 1.0
 * @date 2021/10/25 13:41
 */
public class GroupModelBuilderPlugin implements ModelBuilderPlugin {

    public static final Field TYPE = ReflectionUtils.findField(ModelRef.class, "type");

    public GroupModelBuilderPlugin() {
        TYPE.setAccessible(true);
    }

    @Override
    public void apply(ModelContext context) {
        GroupDocumentationType documentationType = (GroupDocumentationType) context.getDocumentationType();

        Model model = context.getBuilder().build();
        context.getBuilder().id(documentationType.buildModelRefId(model.getId()));
        context.getBuilder().name(documentationType.buildModelRefId(model.getName()));

        // 修改所有非java对象的ref
        for (ModelProperty mp : model.getProperties().values()) {
            boolean refJava = !mp.getModelRef().itemModel().isPresent() && mp.getQualifiedType().startsWith("java.");
            boolean listJava = mp.getModelRef().isCollection() && StringUtils.countOccurrencesOf(mp.getQualifiedType(), "java.") > 1;
            boolean mapJava = mp.getModelRef().isMap() && StringUtils.countOccurrencesOf(mp.getQualifiedType(), "java.") > 2;

            if (refJava || listJava || mapJava) {
                continue;
            }
            if (mp.getModelRef().itemModel().isPresent()) {
                ReflectionUtils.setField(TYPE, mp.getModelRef().itemModel().get(), documentationType.buildModelRefId(mp.getModelRef().itemModel().get().getType()));
            } else {
                ReflectionUtils.setField(TYPE, mp.getModelRef(), documentationType.buildModelRefId(mp.getModelRef().getType()));
            }
        }
    }

    @Override
    public boolean supports(DocumentationType documentationType) {
        return documentationType instanceof GroupDocumentationType;
    }
}
