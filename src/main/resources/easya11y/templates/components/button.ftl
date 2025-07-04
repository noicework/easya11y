[#import "/mmp/includes/templates/class.ftl" as utils]

[#-- Check if hideBottomMargin is enabled --]
[#assign hideBottomMargin = content.hideBottomMargin?? && content.hideBottomMargin]

[#if content.btnText?has_content && content.btnType?has_content]
  [#include "/mmp/includes/templates/href.ftl"]
  [#-- Map variant to BEM modifier --]
  [#assign btnVariantClass = ""]
  [#if content.btnVariant?has_content]
    [#assign btnVariantClass = "mmp-button--${content.btnVariant}"]
  [#else]
    [#assign btnVariantClass = "mmp-button--primary"]
  [/#if]
  
  [#-- Map size to BEM modifier --]
  [#assign btnSizeClass = ""]
  [#if content.btnSize == "sm"]
    [#assign btnSizeClass = "mmp-button--small"]
  [#elseif content.btnSize == "lg"]
    [#assign btnSizeClass = "mmp-button--large"]
  [/#if]
  
  [#-- Build the complete class string --]
  [#assign buttonClass = utils.getClassName("mmp-button ${btnVariantClass} ${btnSizeClass}")]
  [#if content.btnType == 'button']
    <button type="submit"
            class="${buttonClass}"
            style="${cmsfn.decode(content).css!""} ${hideBottomMargin?then('margin-bottom:0;', '')}"
            data-component="button">${content.btnText}</button>
  [#elseif content.btnType == 'input']
    <input type="button"
            class="${buttonClass}"
            style="${cmsfn.decode(content).css!""} ${hideBottomMargin?then('margin-bottom:0;', '')}"
            value="${content.btnText!}">
  [#elseif content.btnType == 'submit']
    <input type="submit"
           class="${buttonClass}"
           style="${cmsfn.decode(content).css!""} ${hideBottomMargin?then('margin-bottom:0;', '')}"
           value="${content.btnText!}">
  [#elseif content.btnType == 'reset']
    <input type="reset"
           class="${buttonClass}"
           style="${cmsfn.decode(content).css!""} ${hideBottomMargin?then('margin-bottom:0;', '')}"
           value="${content.btnText!}">
  [#else]
    <a role="button"
       class="${buttonClass}"
       style="${cmsfn.decode(content).css!""} ${hideBottomMargin?then('margin-bottom:0;', '')}"
       href="${href}">${content.btnText}</a>
  [/#if]
[/#if]