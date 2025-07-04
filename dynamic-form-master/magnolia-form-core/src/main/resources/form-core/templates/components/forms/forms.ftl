[#-- assign part --]
[#assign forms = formfn.getForms()!]
[#if content.link?has_content]
    [#assign formDetailsLink = cmsfn.link("website", content.link!)!]
[/#if]

[#-- render part --]
[#if content.title?has_content]
    <h1>${content.title!}</h1>
[/#if]
[#if content.description?has_content]
    <p>${content.description!}</p>
[/#if]

[#list forms as form]

    [#if formDetailsLink?has_content]
        <a href="${formDetailsLink + "?form=" + form.id!}">
    [/#if]
    [#if form.title?has_content]
        <h4>${form.title!}</h4>
    [/#if]
    [#if form.description?has_content]
        <p>${form.description!}</p>
    [/#if]
    [#if formDetailsLink?has_content]
        </a>
    [/#if]

    <br/><br/>
[/#list]

