[#import "/easya11y/templates/includes/class.ftl" as utils]

[#assign isAuthorMode = cmsfn.editMode!false /]

<div class="form-conditional${isAuthorMode?then(' form-conditional--author', '')}" 
     data-form-conditional
     data-field-to-check="${content.fieldToCheck!""}"
     data-value-to-match="${content.valueToMatch!""}"
     data-operator="${content.operator!"equals"}"
     class="${utils.getClassName(content.class!"")}" 
     style="${content.css!""}">
  [#if isAuthorMode]
    <div class="form-conditional__info">
      <strong>Conditional Rule:</strong> Show when "<em>${content.fieldToCheck!""}</em>" ${content.operator!"equals"} "<em>${content.valueToMatch!""}</em>"
    </div>
  [/#if]
  [@cms.area name="items" contextAttributes={"content": content!} /]
</div>