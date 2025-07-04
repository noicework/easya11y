[#-- include part --]
[#include "/form-core/macros/results.ftl"]

[#-- assign part --]
[#if content.formId?has_content]
    [#assign formId = formfn.parseStringToUuid(content.formId)]
[/#if]
[#assign form = formfn.getForm(formId)!]
[#assign results = formfn.getResults(formId)!]

[#-- render part --]
[@renderFormResults form results /]
