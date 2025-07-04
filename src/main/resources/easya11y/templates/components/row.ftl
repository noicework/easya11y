[#import "/easya11y/templates/includes/class.ftl" as utils]

[#assign colWidths = ['12']]

[#if content.layout?? && content.layout?has_content]
  [#assign colWidths = content.layout?split(' ')]
[/#if]

[#assign rowStyles = ""]
[#if content.backgroundColor?has_content]
  [#assign rowStyles = rowStyles + "background-color: " + content.backgroundColor + "; "]
[/#if]
[#if content.backgroundImage?has_content]
  [#assign rowStyles = rowStyles + "background-image: url('" + ctx.contextPath + content.backgroundImage + "'); background-size: cover; background-position: center; "]
[/#if]
[#if content.textAlign?has_content]
  [#assign rowStyles = rowStyles + "text-align: " + content.textAlign + "; "]
[/#if]

[#assign paddingTop = "p-0"]
[#assign paddingBottom = "p-0"]

[#if content.paddingTop?has_content]
  [#assign paddingTop = content.paddingTop]
[/#if]

[#if content.paddingBottom?has_content]
  [#assign paddingBottom = content.paddingBottom]
[/#if]

<div class="${(content.fullWidth!false)?string('container-fluid', 'container')} ${paddingTop} ${paddingBottom}" style="${rowStyles}">
  <div class="${utils.getClassName('row')}" >
    [#list colWidths as colWidth]
      <div class="grid-col grid-span-${colWidth} ${content.columnClass!}" style="${cmsfn.decode(content).columnCss!""}">
        [@cms.area name="col-${colWidth?index + 1}" /]
      </div>
    [/#list]
  </div>
</div>
