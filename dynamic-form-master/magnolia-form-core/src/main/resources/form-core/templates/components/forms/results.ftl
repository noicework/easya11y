[#-- include part --]
[#include "/form-core/macros/results.ftl"]

[#-- assign part --]
[#assign formId = formfn.parseStringToUuid(ctx.form!)!]
[#if formId?has_content]
    [#assign form = formfn.getForm(formId)!]
    [#assign results = formfn.getResults(formId)!]

    [#-- render part --]
    [@renderFormResults form results /]
[#else]
    <h1>Form uuid parameter is missing!</h1>
[/#if]
