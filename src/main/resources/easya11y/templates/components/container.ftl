[#import "/easya11y/templates/includes/class.ftl" as utils]

[#-- Get the form node path using Magnolia's context --]
[#assign formPath = ""]
[#if cmsfn.page(content)??]
  [#assign formPath = cmsfn.page(content).@path]
[#elseif ctx.currentContent??]
  [#assign formPath = ctx.currentContent.@path]
[/#if]

[#-- Get page properties as fallback for form configuration --]
[#assign pageContent = cmsfn.page(content)!]
[#assign useContentOrPage = {
  "formId": content.formId!pageContent.formId!"",
  "formName": content.formName!pageContent.formName!"",
  "method": content.method!pageContent.method!"post",
  "action": content.action!pageContent.action!"",
  "enctype": content.enctype!pageContent.enctype!"application/x-www-form-urlencoded",
  "successMessage": content.successMessage!pageContent.successMessage!"",
  "errorMessage": content.errorMessage!pageContent.errorMessage!"",
  "redirectUrl": content.redirectUrl!pageContent.redirectUrl!"",
  "redirectDelay": content.redirectDelay!pageContent.redirectDelay!0,
  "submissionType": content.submissionType!pageContent.submissionType!"url",
  "emailTo": content.emailTo!pageContent.emailTo!"",
  "emailSubject": content.emailSubject!pageContent.emailSubject!"",
  "emailFrom": content.emailFrom!pageContent.emailFrom!"",
  "emailRoutingType": content.emailRoutingType!pageContent.emailRoutingType!"static",
  "emailFieldKey": content.emailFieldKey!pageContent.emailFieldKey!"",
  "useMailchimp": content.useMailchimp!pageContent.useMailchimp!false,
  "mailchimpAudienceId": content.mailchimpAudienceId!pageContent.mailchimpAudienceId!"",
  "mailchimpApiKey": content.mailchimpApiKey!pageContent.mailchimpApiKey!"",
  "mailchimpDataCenter": content.mailchimpDataCenter!pageContent.mailchimpDataCenter!"",
  "mailchimpEmailField": content.mailchimpEmailField!pageContent.mailchimpEmailField!"email",
  "formApiEndpoint": content.formApiEndpoint!pageContent.formApiEndpoint!"",
  "useRecaptcha": content.useRecaptcha!pageContent.useRecaptcha!false,
  "recaptchaSiteKey": content.recaptchaSiteKey!pageContent.recaptchaSiteKey!"",
  "recaptchaAction": content.recaptchaAction!pageContent.recaptchaAction!"form_submit",
  "submitButtonText": content.submitButtonText!pageContent.submitButtonText!"Submit",
  "ariaLabelledBy": content.ariaLabelledBy!pageContent.ariaLabelledBy!"",
  "ariaDescribedBy": content.ariaDescribedBy!pageContent.ariaDescribedBy!""
} /]

<div class="easya11y-form-container ${utils.getClassName(content.class!"")}" style="${content.css!""} data-component="form">
  <form id="${useContentOrPage.formId}" 
        name="${useContentOrPage.formName}" 
        method="${useContentOrPage.method}" 
        action="${useContentOrPage.action}" 
        enctype="${useContentOrPage.enctype}" 
        class="needs-validation" 
        novalidate
        data-easya11y-container
        data-form-path="${formPath}"
        data-form-id="${useContentOrPage.formId}"
        data-success-message="${useContentOrPage.successMessage}"
        data-error-message="${useContentOrPage.errorMessage}"
        data-redirect-url="${useContentOrPage.redirectUrl}"
        data-redirect-delay="${useContentOrPage.redirectDelay}"
        [#if useContentOrPage.formApiEndpoint?has_content]
        data-api-endpoint="${useContentOrPage.formApiEndpoint}"
        [/#if]
        [#if useContentOrPage.ariaLabelledBy?has_content]
        aria-labelledby="${useContentOrPage.ariaLabelledBy}"
        [/#if]
        [#if useContentOrPage.ariaDescribedBy?has_content]
        aria-describedby="${useContentOrPage.ariaDescribedBy}"
        [/#if]
        style="${content.css!""}"
        data-submission-config="true">
    
    <!-- Error summary for accessibility - positioned at the top of the form -->
    <div class="validation-summary" style="display: none;" aria-live="assertive" role="alert" tabindex="-1">
      <div class="alert alert-danger">
        <h4>Please correct the following errors:</h4>
        <ul class="validation-error-list"></ul>
      </div>
    </div>
    
    <div class="form-items">
      [@cms.area name="items" contextAttributes={"content": content!} /]
    </div>
    
    <div class="form-submit">
      <input type="submit" class="btn btn-primary" id="${useContentOrPage.formId}-submit" value="${useContentOrPage.submitButtonText}">
    </div>
    
    <div class="form-messages" style="display: none;" aria-live="polite">
      <div class="alert alert-success" role="alert" style="display: none;" tabindex="-1"></div>
      <div class="alert alert-danger" role="alert" style="display: none;" tabindex="-1"></div>
    </div>
    
    <!-- Spinner for loading state -->
    <div class="form-loading" style="display: none;" aria-hidden="true">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">Loading...</span>
      </div>
    </div>
  </form>

  <script src="${ctx.contextPath}/.resources/easya11y/webresources/js/easya11y.js"></script>
  
  [#if useContentOrPage.useRecaptcha && useContentOrPage.recaptchaSiteKey?has_content]
  <script src="https://www.google.com/recaptcha/api.js?render=${useContentOrPage.recaptchaSiteKey}"></script>
  [/#if]
  
  [#-- Minimal form configuration for client-side --]
  <script type="application/json" id="form-config-${useContentOrPage.formId}">
  {
    "formPath": "${formPath}",
    "saveToJcr": true,
    [#if useContentOrPage.useRecaptcha]
    "useRecaptcha": ${useContentOrPage.useRecaptcha?c},
    "recaptchaSiteKey": "${useContentOrPage.recaptchaSiteKey}",
    "recaptchaAction": "${useContentOrPage.recaptchaAction}"
    [#else]
    "useRecaptcha": false
    [/#if]
  }
  </script>
</div>