package com.smartai.common.generator;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.config.MergeConstants;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.internal.DefaultCommentGenerator;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static org.mybatis.generator.internal.util.StringUtility.isTrue;

public class MyCommentGenerator extends DefaultCommentGenerator {
    private Properties properties;

    private Properties systemPro;

    private boolean suppressDate;

    private boolean suppressAllComments;

    private String currentDateStr;

    public MyCommentGenerator() {
        super();
        properties = new Properties();
        systemPro = System.getProperties();
        suppressDate = false;
        suppressAllComments = false;
        currentDateStr = (new SimpleDateFormat("yyyy-MM-dd")).format(new Date());
    }

    public void addComment(XmlElement xmlElement) {
        return;
    }

    public void addConfigurationProperties(Properties properties) {
        this.properties.putAll(properties);
        suppressDate = isTrue(properties.getProperty(PropertyRegistry.COMMENT_GENERATOR_SUPPRESS_DATE));
        suppressAllComments = isTrue(properties.getProperty(PropertyRegistry.COMMENT_GENERATOR_SUPPRESS_ALL_COMMENTS));
    }

    protected void addJavadocTag(JavaElement javaElement, boolean markAsDoNotDelete) {
        javaElement.addJavaDocLine(" *");
        StringBuilder sb = new StringBuilder();
        sb.append(" * ");
        sb.append(MergeConstants.NEW_ELEMENT_TAG);
        if (markAsDoNotDelete) {
            sb.append(" do_not_delete_during_merge");
        }
        String s = getDateString();
        if (s != null) {
            sb.append(' ');
            sb.append(s);
        }
        javaElement.addJavaDocLine(sb.toString());
    }

    protected String getDateString() {
        String result = null;
        if (!suppressDate) {
            result = currentDateStr;
        }
        return result;
    }

    public void addClassComment(InnerClass innerClass, IntrospectedTable introspectedTable) {
        if (suppressAllComments) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        innerClass.addJavaDocLine("/**");
        sb.append(" * ");
        sb.append(introspectedTable.getFullyQualifiedTable());
        sb.append(" ");
        sb.append(getDateString());
        innerClass.addJavaDocLine(sb.toString());
        innerClass.addJavaDocLine(" */");
    }

    public void addEnumComment(InnerEnum innerEnum, IntrospectedTable introspectedTable) {
        if (suppressAllComments) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        innerEnum.addJavaDocLine("/**");
        sb.append(" * ");
        sb.append(introspectedTable.getFullyQualifiedTable());
        innerEnum.addJavaDocLine(sb.toString());
        innerEnum.addJavaDocLine(" */");
    }

    public void addFieldComment(Field field, IntrospectedTable introspectedTable,
        IntrospectedColumn introspectedColumn) {
        if (suppressAllComments) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("// ").append(introspectedColumn.getRemarks());
        field.addJavaDocLine(sb.toString());
        /*  注释 @Column
          @Column(name = "email")
          private String email;

        List<IntrospectedColumn> primaryKeyColumns = introspectedTable.getPrimaryKeyColumns();
        for (IntrospectedColumn col : primaryKeyColumns) {
            if (col.getActualColumnName().equals(introspectedColumn.getActualColumnName())) {
                field.addAnnotation("@Id");
            }
        }
        field.addAnnotation("@Column(name = \"" + introspectedColumn.getActualColumnName() + "\")");
        */
    }

    public void addFieldComment(Field field, IntrospectedTable introspectedTable) {
        if (suppressAllComments) {
            return;
        }

        StringBuilder sb = new StringBuilder();

        field.addJavaDocLine("/**");
        sb.append(" * ");
        sb.append(introspectedTable.getFullyQualifiedTable());
        field.addJavaDocLine(sb.toString());
        field.addJavaDocLine(" */");
    }

    public void addGeneralMethodComment(Method method, IntrospectedTable introspectedTable) {
        if (suppressAllComments) {
            return;
        }
        method.addJavaDocLine("/**");
        StringBuilder sb = new StringBuilder();
        List<Parameter> parms = method.getParameters();
        parms.forEach(parameter -> sb.append(" * @param ").append(parameter.getName()).append("\n"));
        sb.append("     * @return ").append(method.getReturnType());
        method.addJavaDocLine(sb.toString());
        method.addJavaDocLine(" */");
    }

    public void addGetterComment(Method method, IntrospectedTable introspectedTable,
        IntrospectedColumn introspectedColumn) {
        if (suppressAllComments) {
            return;
        }
        //        method.addJavaDocLine("/**");
        //        StringBuilder sb = new StringBuilder();
        //        sb.append(" * ");
        //        sb.append(introspectedColumn.getRemarks());
        //        method.addJavaDocLine(sb.toString());
        //        sb.setLength(0);
        //        sb.append(" * @return ");
        //        sb.append(introspectedColumn.getJavaProperty());
        //        sb.append(" ");
        //        sb.append(introspectedColumn.getRemarks());
        //        method.addJavaDocLine(sb.toString());
        //        method.addJavaDocLine(" */");
    }

    public void addSetterComment(Method method, IntrospectedTable introspectedTable,
        IntrospectedColumn introspectedColumn) {
        if (suppressAllComments) {
            return;
        }
        //        method.addJavaDocLine("/**");
        //        StringBuilder sb = new StringBuilder();
        //        sb.append(" * ");
        //        sb.append(introspectedColumn.getRemarks());
        //        method.addJavaDocLine(sb.toString());
        //        Parameter parm = method.getParameters().get(0);
        //        sb.setLength(0);
        //        sb.append(" * @param ");
        //        sb.append(parm.getName());
        //        sb.append(" ");
        //        sb.append(introspectedColumn.getRemarks());
        //        method.addJavaDocLine(sb.toString());
        //        method.addJavaDocLine(" */");
    }

    @Deprecated
    public void addClassComment(InnerClass innerClass, IntrospectedTable introspectedTable, boolean markAsDoNotDelete) {

    }

    public void addModelClassComment(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        topLevelClass.addImportedType("javax.persistence.Column");
        topLevelClass.addImportedType("javax.persistence.Id");
        topLevelClass.addImportedType("javax.persistence.Table");
        topLevelClass.addImportedType("org.apache.ibatis.type.Alias");

        if (suppressAllComments) {
            return;
        }

        StringBuilder sb = new StringBuilder();

        topLevelClass.addJavaDocLine("/**");
        sb.append(" * ");
        sb.append(introspectedTable.getFullyQualifiedTable());
        topLevelClass.addJavaDocLine(sb.toString());

        sb.setLength(0);
        sb.append(" * @author ");
        sb.append(systemPro.getProperty("user.name"));
        sb.append(" ");
        sb.append(currentDateStr);
        topLevelClass.addJavaDocLine(" */");
        topLevelClass.addAnnotation("@TableName(\"" + introspectedTable.getFullyQualifiedTable() + "\")");
        topLevelClass.addAnnotation(
            "@Alias(\"" + toLowerCaseFirstOne(introspectedTable.getTableConfiguration().getDomainObjectName()) + "\")");
    }

    // 首字母转小写
    public static String toLowerCaseFirstOne(String s) {
        if (Character.isLowerCase(s.charAt(0))) {
            return s;
        } else {
            return (new StringBuilder()).append(Character.toLowerCase(s.charAt(0))).append(s.substring(1)).toString();
        }
    }

    public void addGeneralMethodAnnotation(Method method, IntrospectedTable introspectedTable,
        Set<FullyQualifiedJavaType> imports) {
        System.out.println(method.getName());

    }

    public void addGeneralMethodAnnotation(Method method, IntrospectedTable introspectedTable,
        IntrospectedColumn introspectedColumn, Set<FullyQualifiedJavaType> imports) {
        System.out.println(method.getName());

    }

    public void addFieldAnnotation(Field field, IntrospectedTable introspectedTable,
        Set<FullyQualifiedJavaType> imports) {
        System.out.println(field.getName());

    }

    public void addFieldAnnotation(Field field, IntrospectedTable introspectedTable,
        IntrospectedColumn introspectedColumn, Set<FullyQualifiedJavaType> imports) {
        System.out.println(field.getName());
        field.addAnnotation("@Column(name = \"" + field.getName() + "\")");
    }

    public void addClassAnnotation(InnerClass innerClass, IntrospectedTable introspectedTable,
        Set<FullyQualifiedJavaType> imports) {
        System.out.println(innerClass);
    }
}
