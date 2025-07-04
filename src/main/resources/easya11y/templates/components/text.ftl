[#import "/easya11y/templates/includes/class.ftl" as utils]

<div class="form-group ${utils.getClassName(content.class!"")}" style="${content.css!""}">
  [#if content.label?? || content.helpText??]
    <div class="form-field-header">
      [#if content.label??]
        <label for="${content.name!""}"
               [#if content.required?? && content.required]data-required="true"[/#if]>
          ${content.label!""}
          [#if content.required?? && content.required]<span class="required-marker" aria-hidden="true">*</span>[/#if]
        </label>
      [/#if]

      [#if content.helpText??]
        <div id="help-text-${content.name!""}" class="form-help-text">${content.helpText}</div>
      [/#if]
    </div>
  [/#if]
  
  [#if content.inputType?? && content.inputType == "textarea"]
    <textarea 
         class="form-control" 
         id="${content.name!""}" 
         name="${content.name!""}" 
         [#if content.placeholder??]placeholder="${content.placeholder!""}"[/#if]
         [#if content.required?? && content.required]required aria-required="true"[/#if]
         aria-invalid="false"
         [#if content.helpText??]aria-describedby="help-text-${content.name!""} error-${content.name!""}"[#else]aria-describedby="error-${content.name!""}"[/#if]></textarea>
  [#else]
    <input type="${content.inputType!"text"}" 
         class="form-control" 
         id="${content.name!""}" 
         name="${content.name!""}" 
         [#if content.placeholder??]placeholder="${content.placeholder!""}"[/#if]
         [#if content.pattern??]pattern="${content.pattern!""}"[/#if]
         [#if content.required?? && content.required]required aria-required="true"[/#if]
         aria-invalid="false"
         [#if content.helpText??]aria-describedby="help-text-${content.name!""} error-${content.name!""}"[#else]aria-describedby="error-${content.name!""}"[/#if]>
  [/#if]
  

  
  <div id="error-${content.name!""}" class="invalid-feedback">
    ${content.errorMessage!"Please provide a valid value."}
  </div>
</div>