[#macro renderFormResults form results]

    [#if form?has_content]
        [#if form.title?has_content]
            <h1>${form.title!}</h1>
        [/#if]
        [#if form.description?has_content]
            <p>${form.description!}</p>
        [/#if]


        [#if form.sections?has_content && form.sections?size > 0]
            [#list form.sections?sort_by("orderIndex") as section]
                <h4>${section.title!}</h4><br/><br/>
                [@renderQustions section.questions results /]
            [/#list]
        [/#if]

        [#if form.questions?has_content && form.questions?size > 0]
            <br/><br/>
            [@renderQustions form.questions results /]
        [/#if]


    [#else]
        <p>No form found. Please pass proper form id.</p>
    [/#if]

[/#macro]

[#macro renderQustions questions results]
    [#if questions?has_content && questions?size > 0]
        [#list questions?sort_by("orderIndex") as question]
            [@renderQustion question results /]
        [/#list]
    [/#if]
[/#macro]

[#macro renderQustion question results]
    [#if question?has_content]
        [#if "text" == question.questionType]
            [@renderFreeText question results /]
        [#elseif "single" == question.questionType]
            [@renderChoice question results /]
        [#elseif "multi" == question.questionType]
            [@renderChoice question results /]
        [#elseif "range" == question.questionType]
            [@renderRange question results /]
        [/#if]
        <br/><br/>
    [/#if]
[/#macro]

[#macro renderRange question results]
    <label>${question.question!}</label><br/>
    [#list results as result]
        [#list result.items as item]
            [#if item.questionId == question.id]
                <p>${item.value!}</p>
            [/#if]
        [/#list]
    [/#list]
[/#macro]

[#macro renderFreeText question results]
    <label>${question.question!}</label><br/>
    [#list results as result]
        [#list result.items as item]
            [#if item.questionId == question.id]
                <p>${item.value!}</p>
            [/#if]
        [/#list]
    [/#list]
[/#macro]

[#macro renderChoice question results]
    [#if question.answerOptions?has_content && question.answerOptions?size>0]
        <label>${question.question!}</label><br/>
        [#list question.answerOptions?sort_by("orderIndex") as answerOption]
            [#assign count = 0]
            [#list results as result]
                [#list result.items as item]
                    [#if item.questionId == question.id]
                        [#if item.value == answerOption.value]
                            [#assign count = count + 1]
                        [/#if]
                    [/#if]
                [/#list]
            [/#list]

            <label>${answerOption.label!}:  ${count}</label><br/>
        [/#list]
    [/#if]
[/#macro]
