[#import "/easya11y/templates/includes/class.ftl" as utils]

<div class="form-group ${utils.getClassName(content.class!"")}" style="${content.css!""}">
  [#-- Display warning if name attribute is missing --]
  [#if !content.name?? || content.name?trim == ""]
    <div class="alert alert-warning" role="alert">
      <strong>Warning:</strong> Field name is missing. Form validation requires a name attribute.
    </div>
  [/#if]
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
  
  [#switch content.optionType!"checkbox"]
    [#case "checkbox"]
      [#if content.options??]
        <div class="form-check-group" data-validate="${content.required?c}" role="group" aria-labelledby="group-label-${(content.name?? && content.name?trim != "")?then(content.name, "unnamed-group")}">
          [#if content.label??]
          <span id="group-label-${content.name!""}" class="visually-hidden">${content.label!""}</span>
          [/#if]
          [#list content.options?children as option]
            <div class="form-check">
              <input class="form-check-input" type="checkbox" 
                    name="${(content.name?? && content.name?trim != "")?then(content.name, "")}" 
                    id="${(content.name?? && content.name?trim != "")?then(content.name, "checkbox")}_${option?node_name}"
                    value="${option?is_hash?then(option.value!"", option?string)}"
                    [#if content.required?? && content.required]required aria-required="true"[/#if]
                    aria-invalid="false">
              <label class="form-check-label" for="${content.name!""}_${option?node_name}">
                ${option?is_hash?then(option.label!option.value!"", option?string)}
              </label>
            </div>
          [/#list]
        </div>
        <div id="error-${content.name!""}" class="invalid-feedback">
          ${content.errorMessage!"Please select at least one option."}
        </div>
      [/#if]
      [#break]
    
    [#case "radio"]
      [#if content.options??]
        <div class="form-radio-group" data-validate="${content.required?c}" role="radiogroup" aria-labelledby="group-label-${(content.name?? && content.name?trim != "")?then(content.name, "unnamed-group")}">
          [#if content.label??]
          <span id="group-label-${content.name!""}" class="visually-hidden">${content.label!""}</span>
          [/#if]
          [#list content.options?children as option]
            <div class="form-check">
              <input class="form-check-input" type="radio" 
                    name="${(content.name?? && content.name?trim != "")?then(content.name, "")}" 
                    id="${(content.name?? && content.name?trim != "")?then(content.name, "radio")}_${option?node_name}"
                    value="${option?is_hash?then(option.value!"", option?string)}"
                    [#if content.required?? && content.required]required aria-required="true"[/#if]
                    aria-invalid="false">
              <label class="form-check-label" for="${content.name!""}_${option?node_name}">
                ${option?is_hash?then(option.label!option.value!"", option?string)}
              </label>
            </div>
          [/#list]
        </div>
        <div id="error-${content.name!""}" class="invalid-feedback">
          ${content.errorMessage!"Please select an option."}
        </div>
      [/#if]
      [#break]
    
    [#case "drop-down"]
      <select class="form-select" 
              name="${content.name!""}"
              id="${content.name!""}"
              [#if content.required?? && content.required]required aria-required="true"[/#if]
              [#if content.placeholder??]data-placeholder="${content.placeholder!"Select an option"}"[/#if]
              aria-invalid="false"
              aria-describedby="error-${content.name!""}">
        [#if content.placeholder??]
          <option value="">${content.placeholder!"Select an option"}</option>
        [/#if]
        [#if content.options??]
          [#list content.options?children as option]
            <option value="${option?is_hash?then(option.value!"", option?string)}">
              ${option?is_hash?then(option.label!option.value!"", option?string)}
            </option>
          [/#list]
        [/#if]
      </select>
      [#break]
    
    [#case "multi-drop-down"]
      <select class="form-select" 
              name="${content.name!""}[]" 
              id="${content.name!""}"
              multiple
              [#if content.required?? && content.required]required aria-required="true"[/#if]
              [#if content.placeholder??]data-placeholder="${content.placeholder!"Select options"}"[/#if]
              aria-invalid="false"
              aria-describedby="error-${content.name!""}">
        [#if content.options??]
          [#list content.options?children as option]
            <option value="${option?is_hash?then(option.value!"", option?string)}">
              ${option?is_hash?then(option.label!option.value!"", option?string)}
            </option>
          [/#list]
        [/#if]
      </select>
      [#break]
  [/#switch]
  
</div>