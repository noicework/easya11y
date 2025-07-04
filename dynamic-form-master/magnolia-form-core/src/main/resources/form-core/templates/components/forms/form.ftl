[#-- include part --]
[#include "/form-core/macros/form.ftl"]

[#-- assign part --]
[#assign formId = formfn.parseStringToUuid(ctx.form!)!]
[#if formId?has_content]
    [#assign form = formfn.getForm(formId)!]

    [#if content.link?has_content]
        [#assign formResultsLink = cmsfn.link("website", content.link!)!]
    [/#if]

    [#-- render part --]
    [@renderForm form content.@id formResultsLink /]
[#else]
    <h1>Form uuid parameter is missing!</h1>
[/#if]
