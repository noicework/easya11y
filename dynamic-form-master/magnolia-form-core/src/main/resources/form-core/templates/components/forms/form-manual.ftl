[#-- include part --]
[#include "/form-core/macros/form.ftl"]

[#-- assign part --]
[#if content.formId?has_content]
    [#assign formId = formfn.parseStringToUuid(content.formId)]
[/#if]
[#assign form = formfn.getForm(formId)!]

[#if content.link?has_content]
    [#assign formResultsLink = cmsfn.link("website", content.link!)!]
[/#if]

[#-- render part --]
[@renderForm form content.@id formResultsLink /]
