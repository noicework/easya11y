/**
 * Form validation and submission handler with Mailchimp integration
 * Enhanced for WCAG 2.1 AA accessibility
 */
import * as yup from 'yup';

class FormHandler {
  constructor() {
    // Look for forms with data-easya11y-container or any form in form-embed-container
    this.forms = document.querySelectorAll('[data-easya11y-container], .form-embed-container form, form[data-form-path], form[data-form-id]');
    this.validationSchemas = new Map();
    this.isAuthorMode = document.body.classList.contains('easya11y-author-mode');
    console.log('FormHandler initialized, found forms:', this.forms.length);
    console.log('Forms found:', this.forms);
    
    this.init();
  }

  init() {
    document.body.classList.add('easya11y-init');
    
    // Dispatch global initialization event
    this.dispatchEvent(document, 'easya11y:initialized', {
      formsCount: this.forms.length,
      isAuthorMode: this.isAuthorMode
    });
    
    if (this.forms.length) {
      this.forms.forEach(form => {
        this.setupFormValidation(form);
        this.setupFormSubmission(form);
        this.setupConditionalFields(form);
        this.setupAccessibility(form);
        
        // Dispatch form setup event
        const formId = form.getAttribute('id') || form.getAttribute('data-form-id');
        const formPath = form.getAttribute('data-form-path');
        this.dispatchEvent(form, 'easya11y:form:setup', {
          form,
          formId,
          formPath,
          config: this.getFormConfig(form)
        });
      });
    }
  }

  /**
   * Dispatch a custom event with detail payload
   * @param {EventTarget} target - The element to dispatch the event from
   * @param {string} eventName - The name of the event
   * @param {Object} detail - The event detail payload
   * @param {boolean} cancelable - Whether the event can be canceled
   * @returns {boolean} - Whether the event was not canceled
   */
  dispatchEvent(target, eventName, detail = {}, cancelable = false) {
    const event = new CustomEvent(eventName, {
      detail,
      bubbles: true,
      cancelable
    });
    return target.dispatchEvent(event);
  }

  /**
   * Get form configuration from script tag or data attributes
   * @param {HTMLFormElement} form - The form element
   * @returns {Object} - The form configuration
   */
  getFormConfig(form) {
    let formConfig = {};
    const formId = form.getAttribute('id') || form.getAttribute('data-form-id');
    const configScript = document.getElementById(`form-config-${formId}`);
    
    if (configScript) {
      try {
        formConfig = JSON.parse(configScript.textContent);
      } catch (e) {
        console.error('Failed to parse form config:', e);
      }
    }
    
    // Merge with data attributes - only keeping essential client-side configs
    return {
      ...formConfig,
      saveToJcr: formConfig.saveToJcr !== false && form.getAttribute('data-save-to-jcr') !== 'false',
      useRecaptcha: formConfig.useRecaptcha || form.getAttribute('data-use-recaptcha') === 'true',
      recaptchaSiteKey: formConfig.recaptchaSiteKey || form.getAttribute('data-recaptcha-site-key'),
      recaptchaAction: formConfig.recaptchaAction || form.getAttribute('data-recaptcha-action') || 'form_submit',
      formPath: formConfig.formPath || form.getAttribute('data-form-path')
    };
  }

  /**
   * Set up form validation
   * @param {HTMLFormElement} form - The form element to validate
   */
  setupFormValidation(form) {
    // Build validation schema for this form
    const schema = this.buildValidationSchema(form);
    this.validationSchemas.set(form, schema);

    // Note: The form submission is now handled in setupFormSubmission
    // We no longer add a separate submit event listener here to prevent duplicates

    // Add validation for individual fields on blur
    const inputs = form.querySelectorAll('input, select, textarea');
    inputs.forEach(input => {
      input.addEventListener('blur', () => {
        this.validateField(input, form);
      });
    });
  }

  /**
   * Build a Yup validation schema for the form
   * @param {HTMLFormElement} form - The form to build a schema for
   * @returns {Object} Yup validation schema
   */
  buildValidationSchema(form) {
    const schemaFields = {};
    const inputs = form.querySelectorAll('input, select, textarea');

    inputs.forEach(input => {
      const name = input.name;
      if (!name) return;

      let fieldSchema;
      const isRequired = input.hasAttribute('required');

      // Create schema based on input type
      switch (input.type) {
        case 'email':
          fieldSchema = yup.string().email('Please enter a valid email address');
          break;
        case 'number':
          fieldSchema = yup.number().typeError('Please enter a valid number');

          // Add min/max validation if attributes exist
          if (input.hasAttribute('min')) {
            fieldSchema = fieldSchema.min(
              parseFloat(input.getAttribute('min')),
              `Value must be at least ${input.getAttribute('min')}`
            );
          }

          if (input.hasAttribute('max')) {
            fieldSchema = fieldSchema.max(
              parseFloat(input.getAttribute('max')),
              `Value cannot exceed ${input.getAttribute('max')}`
            );
          }
          break;
        case 'tel':
          fieldSchema = yup.string().matches(
            /^[+]?[(]?[0-9]{3}[)]?[-\s.]?[0-9]{3}[-\s.]?[0-9]{4,6}$/,
            'Please enter a valid phone number'
          );
          break;
        case 'url':
          fieldSchema = yup.string().url('Please enter a valid URL');
          break;
        case 'date':
          fieldSchema = yup.date().typeError('Please enter a valid date');
          break;
        case 'checkbox':
          // For checkbox groups, this will be handled separately
          if (input.closest('.form-check-group')) {
            // Skip individual checkboxes in groups - we'll handle the group as a whole
            return;
          }
          fieldSchema = yup.boolean();
          break;
        case 'radio':
          // For radio groups, skip individual inputs - we'll handle the group as a whole
          if (input.closest('.form-radio-group')) {
            return;
          }
          fieldSchema = yup.string();
          break;
        default:
          // Handle text, textarea, select, etc.
          fieldSchema = yup.string();

          // Add pattern validation if available
          if (input.hasAttribute('pattern')) {
            const pattern = new RegExp(input.getAttribute('pattern'));
            fieldSchema = fieldSchema.matches(
              pattern,
              input.getAttribute('title') || 'Please match the requested format'
            );
          }

          // Add minlength/maxlength if available
          if (input.hasAttribute('minlength')) {
            const minLength = parseInt(input.getAttribute('minlength'), 10);
            fieldSchema = fieldSchema.min(
              minLength,
              `Must be at least ${minLength} characters`
            );
          }

          if (input.hasAttribute('maxlength')) {
            const maxLength = parseInt(input.getAttribute('maxlength'), 10);
            fieldSchema = fieldSchema.max(
              maxLength,
              `Cannot exceed ${maxLength} characters`
            );
          }
      }

      // Add required validation if needed
      if (isRequired && fieldSchema) {
        fieldSchema = fieldSchema.required('This field is required');
      }

      // Only add the schema if we created one
      if (fieldSchema) {
        schemaFields[name] = fieldSchema;
      }
    });

    // Handle special group cases (checkboxes, radios)
    const checkboxGroups = form.querySelectorAll('.form-check-group');
    checkboxGroups.forEach(group => {
      const required = group.getAttribute('data-validate') === 'true';
      const groupName = group.querySelector('input[type="checkbox"]')?.name;

      if (groupName && required) {
        // For checkbox groups, we validate that at least one is checked
        schemaFields[groupName] = yup.array().min(1, 'Please select at least one option');
      }
    });

    const radioGroups = form.querySelectorAll('.form-radio-group');
    radioGroups.forEach(group => {
      const required = group.getAttribute('data-validate') === 'true';
      const groupName = group.querySelector('input[type="radio"]')?.name;

      if (groupName && required) {
        // For radio groups, we validate that one option is selected
        schemaFields[groupName] = yup.string().required('Please select an option');
      }
    });

    return yup.object().shape(schemaFields);
  }

  /**
   * Validate an individual form field
   * @param {HTMLElement} field - The input field to validate
   * @param {HTMLFormElement} form - The parent form
   * @returns {boolean} Whether the field is valid
   */
  validateField(field, form) {
    const schema = this.validationSchemas.get(form);
    if (!schema || !field.name) return true;

    // Dispatch before validation event
    this.dispatchEvent(form, 'easya11y:field:validate', {
      form,
      field,
      fieldName: field.name
    });

    let isValid = true;
    let errorMessage = '';
    let fieldValue;

    try {
      // Get the field schema
      const fieldSchema = schema.fields[field.name];
      if (!fieldSchema) return true;

      fieldValue = field.value;

      // Special handling for checkboxes and radio buttons
      if (field.type === 'checkbox') {
        if (field.closest('.form-check-group')) {
          // For checkbox groups, collect all checked values
          const groupInputs = form.querySelectorAll(`input[name="${field.name}"]:checked`);
          fieldValue = Array.from(groupInputs).map(input => input.value);
        } else {
          // For single checkboxes, use checked state
          fieldValue = field.checked;
        }
      } else if (field.type === 'radio' && field.closest('.form-radio-group')) {
        // For radio groups, get the selected value
        const checkedInput = form.querySelector(`input[name="${field.name}"]:checked`);
        fieldValue = checkedInput ? checkedInput.value : '';
      }

      // Validate using Yup
      fieldSchema.validateSync(fieldValue);
    } catch (error) {
      isValid = false;
      errorMessage = error.message;
    }

    // Update UI based on validation result
    this.updateFieldValidationUI(field, isValid, errorMessage);
    
    // Dispatch after validation event
    this.dispatchEvent(form, 'easya11y:field:validated', {
      form,
      field,
      fieldName: field.name,
      isValid,
      errorMessage,
      value: fieldValue
    });
    
    return isValid;
  }

  /**
   * Update the field's validation UI with accessibility enhancements
   * @param {HTMLElement} field - The field to update
   * @param {boolean} isValid - Whether the field is valid
   * @param {string} errorMessage - The error message to display if invalid
   */
  updateFieldValidationUI(field, isValid, errorMessage) {
    // Handle groups
    if (field.type === 'checkbox' && field.closest('.form-check-group')) {
      const group = field.closest('.form-check-group');
      const feedbackElement = group.nextElementSibling;

      if (feedbackElement && feedbackElement.classList.contains('invalid-feedback')) {
        feedbackElement.textContent = errorMessage;

        // For accessibility, associate error with the field
        if (!isValid) {
          // Generate unique ID for the error message if needed
          if (!feedbackElement.id) {
            feedbackElement.id = `error-${field.name}-${Date.now()}`;
          }

          // Associate error with all checkboxes in the group
          const checkboxes = group.querySelectorAll('input[type="checkbox"]');
          checkboxes.forEach(checkbox => {
            checkbox.setAttribute('aria-describedby', feedbackElement.id);
            checkbox.setAttribute('aria-invalid', 'true');
          });
        } else {
          // Remove error association
          const checkboxes = group.querySelectorAll('input[type="checkbox"]');
          checkboxes.forEach(checkbox => {
            checkbox.removeAttribute('aria-describedby');
            checkbox.setAttribute('aria-invalid', 'false');
          });
        }
      }

      // Update group UI
      if (!isValid) {
        group.classList.add('is-invalid');
        group.classList.remove('is-valid');
      } else {
        group.classList.remove('is-invalid');
        group.classList.add('is-valid');
      }
      return;
    }

    if (field.type === 'radio' && field.closest('.form-radio-group')) {
      const group = field.closest('.form-radio-group');
      const feedbackElement = group.nextElementSibling;

      if (feedbackElement && feedbackElement.classList.contains('invalid-feedback')) {
        feedbackElement.textContent = errorMessage;

        // For accessibility, associate error with the field
        if (!isValid) {
          // Generate unique ID for the error message if needed
          if (!feedbackElement.id) {
            feedbackElement.id = `error-${field.name}-${Date.now()}`;
          }

          // Associate error with all radio buttons in the group
          const radios = group.querySelectorAll('input[type="radio"]');
          radios.forEach(radio => {
            radio.setAttribute('aria-describedby', feedbackElement.id);
            radio.setAttribute('aria-invalid', 'true');
          });
        } else {
          // Remove error association
          const radios = group.querySelectorAll('input[type="radio"]');
          radios.forEach(radio => {
            radio.removeAttribute('aria-describedby');
            radio.setAttribute('aria-invalid', 'false');
          });
        }
      }

      // Update group UI
      if (!isValid) {
        group.classList.add('is-invalid');
        group.classList.remove('is-valid');
      } else {
        group.classList.remove('is-invalid');
        group.classList.add('is-valid');
      }
      return;
    }

    // Update individual field UI
    if (field.classList.contains('form-control') || field.classList.contains('form-select')) {
      // Set validation message
      field.setCustomValidity(isValid ? '' : errorMessage);

      // Update classes
      if (!isValid) {
        field.classList.add('is-invalid');
        field.classList.remove('is-valid');
      } else {
        field.classList.remove('is-invalid');
        field.classList.add('is-valid');
      }

      // Update feedback message
      const feedbackElement = field.nextElementSibling;
      if (feedbackElement && feedbackElement.classList.contains('invalid-feedback')) {
        feedbackElement.textContent = errorMessage;

        // For accessibility, associate error with the field
        if (!isValid) {
          // Generate unique ID for the error message if needed
          if (!feedbackElement.id) {
            feedbackElement.id = `error-${field.name}-${Date.now()}`;
          }
          field.setAttribute('aria-describedby', feedbackElement.id);
          field.setAttribute('aria-invalid', 'true');
        } else {
          field.removeAttribute('aria-describedby');
          field.setAttribute('aria-invalid', 'false');
        }
      }
    }
  }

  /**
   * Validate the entire form
   * @param {HTMLFormElement} form - The form to validate
   * @returns {boolean} Whether the form is valid
   */
  validateForm(form) {
    const schema = this.validationSchemas.get(form);
    if (!schema) return true;

    // Dispatch before validation event
    this.dispatchEvent(form, 'easya11y:form:validate', {
      form
    });

    const formData = new FormData(form);
    const data = Object.fromEntries(formData);

    // Special handling for checkbox groups (they need to be arrays)
    const checkboxGroups = form.querySelectorAll('.form-check-group');
    checkboxGroups.forEach(group => {
      const checkboxes = group.querySelectorAll('input[type="checkbox"]');
      if (checkboxes.length && checkboxes[0].name) {
        const name = checkboxes[0].name;
        const checkedValues = Array.from(checkboxes)
          .filter(cb => cb.checked)
          .map(cb => cb.value);
        data[name] = checkedValues;
      }
    });

    // Get all inputs to validate
    const inputs = form.querySelectorAll('input, select, textarea');
    const errors = [];
    let hasErrors = false;

    // Validate each input that is not hidden
    inputs.forEach(input => {
      // Skip hidden inputs or inputs in hidden containers
      const isInHiddenContainer =
        input.closest('[aria-hidden="true"]') ||
        input.closest('[style*="display: none"]') ||
        input.closest('[style*="display:none"]');

      if (input.type !== 'hidden' && !input.disabled && !isInHiddenContainer) {
        const isValid = this.validateField(input, form);

        if (!isValid) {
          hasErrors = true;

          // Get error message
          const fieldSchema = schema.fields[input.name];
          let errorMessage = '';
          try {
            let fieldValue = input.value;

            if (input.type === 'checkbox') {
              if (input.closest('.form-check-group')) {
                const groupInputs = form.querySelectorAll(`input[name="${input.name}"]:checked`);
                fieldValue = Array.from(groupInputs).map(input => input.value);
              } else {
                fieldValue = input.checked;
              }
            } else if (input.type === 'radio' && input.closest('.form-radio-group')) {
              const checkedInput = form.querySelector(`input[name="${input.name}"]:checked`);
              fieldValue = checkedInput ? checkedInput.value : '';
            }

            fieldSchema.validateSync(fieldValue);
          } catch (err) {
            errorMessage = err.message;
          }

          // Add to errors collection for summary
          const label = this.findLabelForField(input);
          const labelText = label ? label.textContent.replace(/\*|\(required\)/g, '').trim() : input.name;

          // For checkbox/radio options, only show the group label in the error summary
          if ((input.type === 'checkbox' && input.closest('.form-check-group')) ||
            (input.type === 'radio' && input.closest('.form-radio-group'))) {
            // Find the group
            const group = input.closest('.form-check-group') || input.closest('.form-radio-group');

            // Find the group label if available
            const groupLabel = group.querySelector('legend') || group.previousElementSibling;
            const groupLabelText = groupLabel ?
              groupLabel.textContent.replace(/\*|\(required\)/g, '').trim() :
              labelText;

            // Check if this group is already in the errors collection
            const existingError = errors.find(err => err.name === input.name);
            if (!existingError) {
              errors.push({
                id: input.id || `field-${input.name}`,
                name: input.name,
                label: groupLabelText,
                message: errorMessage
              });
            }
          } else {
            errors.push({
              id: input.id || `field-${input.name}`,
              name: input.name,
              label: labelText,
              message: errorMessage
            });
          }

          // Ensure field has an ID for the error link
          if (!input.id) {
            input.id = `field-${input.name}`;
          }
        }
      }
    });

    // Dispatch after validation event
    this.dispatchEvent(form, 'easya11y:form:validated', {
      form,
      isValid: !hasErrors,
      errors: errors
    });

    if (!hasErrors) {
      // Hide validation summary if it was previously shown
      const validationSummary = form.querySelector('.validation-summary');
      if (validationSummary) {
        validationSummary.style.display = 'none';
      }
      return true;
    } else {
      // Show validation summary
      this.showValidationSummary(form, errors);

      // Focus the validation summary for accessibility
      const validationSummary = form.querySelector('.validation-summary');
      if (validationSummary) {
        validationSummary.focus();
      } else {
        // Fallback: focus the first invalid field
        const firstInvalidField = form.querySelector('.is-invalid');
        if (firstInvalidField) {
          firstInvalidField.focus();
        }
      }

      // Validation errors will be announced by screen readers through the aria-live region

      return false;
    }
  }

  /**
   * Show validation summary with clickable error links
   * @param {HTMLFormElement} form - The form containing errors
   * @param {Array} errors - The collection of error objects
   */
  showValidationSummary(form, errors) {
    if (!errors || errors.length === 0) return;

    let validationSummary = form.querySelector('.validation-summary');
    let errorList;

    // Create validation summary if it doesn't exist
    if (!validationSummary) {
      validationSummary = document.createElement('div');
      validationSummary.classList.add('validation-summary');
      validationSummary.setAttribute('role', 'alert');
      validationSummary.setAttribute('tabindex', '-1');

      const heading = document.createElement('h3');
      heading.classList.add('validation-heading');
      heading.textContent = 'Please correct the following errors:';

      errorList = document.createElement('ul');
      errorList.classList.add('validation-error-list');

      validationSummary.appendChild(heading);
      validationSummary.appendChild(errorList);

      // Insert at the top of the form
      form.insertBefore(validationSummary, form.firstChild);
    } else {
      errorList = validationSummary.querySelector('.validation-error-list');

      // Create error list if it doesn't exist
      if (!errorList) {
        errorList = document.createElement('ul');
        errorList.classList.add('validation-error-list');
        validationSummary.appendChild(errorList);
      }
    }

    // Clear previous errors
    errorList.innerHTML = '';

    // Add error items with links to fields
    errors.forEach(error => {
      const li = document.createElement('li');
      const link = document.createElement('a');
      link.href = `#${error.id}`;
      link.textContent = error.label;
      link.addEventListener('click', (e) => {
        e.preventDefault();
        const field = document.getElementById(error.id);
        if (field) {
          field.focus();
        }
      });
      li.appendChild(link);
      // Add the error message as plain text after the link
      const messageText = document.createTextNode(`: ${error.message}`);
      li.appendChild(messageText);
      errorList.appendChild(li);
    });

    // Show the summary
    validationSummary.style.display = 'block';

    // Scroll to the top of the form
    validationSummary.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }

  /**
   * Set up form submission handling with AJAX
   * Includes Mailchimp integration when enabled
   * @param {HTMLFormElement} form - The form to handle
   */
  setupFormSubmission(form) {
    console.log('Setting up form submission for form:', form);
    form.addEventListener('submit', async event => {
      console.log('Form submit event triggered');
      // Always prevent the default form submission behavior
      event.preventDefault();
      event.stopPropagation();

      // Add validated class for CSS styling
      form.classList.add('was-validated');

      // Reset any previous error messages
      const errorAlert = form.querySelector('.alert-danger');
      if (errorAlert) {
        errorAlert.style.display = 'none';
        errorAlert.textContent = '';
      }

      // Hide any previous validation summary
      const validationSummary = form.querySelector('.validation-summary');
      if (validationSummary) {
        validationSummary.style.display = 'none';
      }

      // Validate the form before submission
      console.log('Validating form before submission');
      const isValid = this.validateForm(form);
      console.log('Form validation result:', isValid);
      
      if (!isValid) {
        console.log('Form has validation errors, stopping submission');
        // The form has validation errors
        // The validation summary has already been displayed and focused by validateForm
        return false;
      }
      
      console.log('Form validation passed, proceeding with submission');

      // Form is valid, proceed with submission
      const formData = new FormData(form);
      
      // Get form configuration - only essential client-side configs
      const formConfig = this.getFormConfig(form);
      
      // Extract essential config values
      const saveToJcr = formConfig.saveToJcr;
      const useRecaptcha = formConfig.useRecaptcha;
      const recaptchaSiteKey = formConfig.recaptchaSiteKey;
      const recaptchaAction = formConfig.recaptchaAction;
      const formPath = formConfig.formPath;
      const apiEndpoint = form.getAttribute('data-api-endpoint');
      const action = apiEndpoint || form.getAttribute('action') || window.location.href;
      const method = form.getAttribute('method') || 'POST';
      
      // Dispatch before submit event (cancelable)
      const beforeSubmitAllowed = this.dispatchEvent(form, 'easya11y:form:beforeSubmit', {
        form,
        formData,
        config: formConfig
      }, true);
      
      if (!beforeSubmitAllowed) {
        console.log('Form submission canceled by beforeSubmit event');
        return false;
      }
      
      // Debug logging
      console.log('Form submission configuration:', {
        saveToJcr,
        formPath,
        action,
        method,
        useRecaptcha: useRecaptcha ? 'enabled' : 'disabled'
      });

      // Show loading state
      this.setFormLoadingState(form, true);
      
      // Dispatch submitting event
      this.dispatchEvent(form, 'easya11y:form:submitting', {
        form,
        formData,
        config: formConfig
      });

      try {
        // If reCAPTCHA is enabled, generate token
        if (useRecaptcha && recaptchaSiteKey) {
          try {
            const token = await this.generateRecaptchaToken(recaptchaSiteKey, recaptchaAction);
            formData.append('recaptchaToken', token);
          } catch (error) {
            console.error('Failed to generate reCAPTCHA token:', error);
            throw new Error('Security verification failed. Please try again.');
          }
        }

        let submissionMade = false;

        // If JCR storage is enabled, save to JCR (server handles all routing)
        if (saveToJcr && formPath) {
          console.log('Submitting to JCR...');
          
          const jcrResponse = await this.saveFormSubmissionToJcr(formPath, formData);
          console.log('JCR submission response:', jcrResponse);
          this.handleFormSuccess(form, jcrResponse);
          submissionMade = true;
        }

        // Submit the form to the action endpoint (if different from JCR endpoint)
        if (!submissionMade && action && action !== window.location.href) {
          const response = await fetch(action, {
            method: method.toUpperCase(),
            body: formData,
            headers: {
              'X-Requested-With': 'XMLHttpRequest'
            }
          });

          if (!response.ok) {
            throw new Error(`Form submission failed with status: ${response.status}`);
          }

          const data = await response.json();
          this.handleFormSuccess(form, data);
          submissionMade = true;
        }

        // Fallback: submit to default endpoint
        if (!submissionMade) {
          console.log('Using fallback submission endpoint');
          
          // Try to submit to the default form submission endpoint
          const fallbackUrl = `${window.location.origin}${window.location.pathname.includes('magnolia') ? '/magnoliaAuthor' : ''}/.rest/easya11y/submit`;
          
          // If no formPath, try to derive one from the current page
          const currentPath = window.location.pathname;
          const fallbackFormPath = formPath || currentPath;
          
          if (fallbackFormPath) {
            // Add formPath to formData
            formData.append('formPath', fallbackFormPath);
          }
          
          const response = await fetch(fallbackUrl, {
            method: 'POST',
            body: formData,
            headers: {
              'X-Requested-With': 'XMLHttpRequest'
            }
          });

          if (response.ok) {
            const data = await response.json();
            this.handleFormSuccess(form, data);
            submissionMade = true;
          } else {
            console.warn('Fallback submission failed, showing success anyway');
            this.handleFormSuccess(form, { success: true });
          }
        }
      } catch (error) {
        this.handleFormError(form, error);
      } finally {
        this.setFormLoadingState(form, false);
      }

      return false; // Ensure no default form submission happens
    });
  }
  
  /**
   * Generate reCAPTCHA v3 token
   * @param {string} siteKey - The reCAPTCHA site key
   * @param {string} action - The action for this reCAPTCHA request
   * @returns {Promise<string>} - Promise resolving to the reCAPTCHA token
   */
  async generateRecaptchaToken(siteKey, action) {
    return new Promise((resolve, reject) => {
      if (typeof grecaptcha === 'undefined') {
        reject(new Error('reCAPTCHA script not loaded'));
        return;
      }

      grecaptcha.ready(() => {
        grecaptcha.execute(siteKey, { action })
          .then(token => resolve(token))
          .catch(error => reject(error));
      });
    });
  }

  /**
   * Get CSRF token from cookie
   * @returns {string|null} - The CSRF token or null if not found
   */
  getCsrfToken() {
    const name = 'csrf=';
    const decodedCookie = decodeURIComponent(document.cookie);
    const cookies = decodedCookie.split(';');
    
    for (let i = 0; i < cookies.length; i++) {
      let cookie = cookies[i].trim();
      if (cookie.indexOf(name) === 0) {
        return cookie.substring(name.length);
      }
    }
    return null;
  }

  /**
   * Save form submission to JCR via the FormSubmissionEndpoint
   * Server-side will handle all routing configuration lookup
   * @param {string} formPath - The path to the form in the JCR
   * @param {FormData} formData - The form data to save
   * @returns {Promise} - Promise resolving when submission completes
   */
  async saveFormSubmissionToJcr(formPath, formData) {
    // Create URLSearchParams for application/x-www-form-urlencoded
    const submissionData = new URLSearchParams();
    
    // Add the form path - server will lookup all configuration
    submissionData.append('formPath', formPath);
    
    // Convert form data to formData[field] format for our endpoint
    for (const [key, value] of formData.entries()) {
      submissionData.append(`formData[${key}]`, value);
    }
    
    // Submit to our JCR endpoint
    // Build the correct endpoint URL with context
    const baseUrl = window.location.origin;
    const pathParts = window.location.pathname.split('/');
    const contextPath = pathParts[1] && pathParts[1].includes('magnolia') ? `/${pathParts[1]}` : '';
    const endpointUrl = `${baseUrl}${contextPath}/.rest/easya11y/submit`;
    
    console.log('Submitting to endpoint:', endpointUrl);
    
    // Get CSRF token
    const csrfToken = this.getCsrfToken();
    const headers = {
      'Content-Type': 'application/x-www-form-urlencoded',
      'X-Requested-With': 'XMLHttpRequest'
    };
    
    // Add CSRF token header if available
    if (csrfToken) {
      headers['X-CSRF-Token'] = csrfToken;
      console.log('Adding CSRF token to request');
    }
    
    const response = await fetch(endpointUrl, {
      method: 'POST',
      body: submissionData.toString(),
      headers: headers
    });
    
    if (!response.ok) {
      throw new Error(`JCR form submission failed with status: ${response.status}`);
    }
    
    return await response.json();
  }


  /**
   * Set the form's loading state
   * @param {HTMLFormElement} form - The form element
   * @param {boolean} isLoading - Whether the form is in loading state
   */
  setFormLoadingState(form, isLoading) {
    const submitButton = form.querySelector('button[type="submit"]');
    const loadingIndicator = form.querySelector('.form-loading');

    if (isLoading) {
      // Dispatch loading start event
      this.dispatchEvent(form, 'easya11y:loading:start', {
        form
      });
      // Disable submit button and show loading state
      if (submitButton) {
        submitButton.disabled = true;
        submitButton.setAttribute('aria-busy', 'true');

        // Store original text to restore later
        if (!submitButton.dataset.originalText) {
          submitButton.dataset.originalText = submitButton.textContent;
        }
        submitButton.textContent = 'Submitting...';
      }

      // Show loading indicator if it exists
      if (loadingIndicator) {
        loadingIndicator.style.display = 'flex';
      }

      // Loading state is communicated through aria-busy attribute
    } else {
      // Re-enable submit button and restore text
      if (submitButton) {
        submitButton.disabled = false;
        submitButton.setAttribute('aria-busy', 'false');

        // Restore original text if available
        if (submitButton.dataset.originalText) {
          submitButton.textContent = submitButton.dataset.originalText;
        }
      }

      // Hide loading indicator
      if (loadingIndicator) {
        loadingIndicator.style.display = 'none';
      }
      
      // Dispatch loading end event
      this.dispatchEvent(form, 'easya11y:loading:end', {
        form
      });
    }
  }

  /**
   * Handle successful form submission
   * @param {HTMLFormElement} form - The form element
   * @param {Object} response - The response data
   */
  handleFormSuccess(form, response) {
    console.log('handleFormSuccess called with:', response);
    
    // Dispatch success event
    this.dispatchEvent(form, 'easya11y:form:submitted', {
      form,
      response,
      formData: new FormData(form)
    });
    
    // Show success message
    const successMessage = form.getAttribute('data-success-message') || 'Form submitted successfully!';
    
    // Look for existing message container or create one if it doesn't exist
    let messageContainer = form.querySelector('.form-messages');
    let successAlert = form.querySelector('.alert-success');
    
    console.log('Message container:', messageContainer);
    console.log('Success alert:', successAlert);
    
    // If the message container doesn't exist, create it
    if (!messageContainer) {
      messageContainer = document.createElement('div');
      messageContainer.className = 'form-messages';
      // Insert at the beginning of the form
      form.insertBefore(messageContainer, form.firstChild);
    }
    
    // If the success alert doesn't exist, create it
    if (!successAlert) {
      successAlert = document.createElement('div');
      successAlert.className = 'alert alert-success';
      successAlert.setAttribute('role', 'alert');
      successAlert.setAttribute('tabindex', '-1');
      messageContainer.appendChild(successAlert);
    }
    
    // Update and display the success message
    successAlert.textContent = successMessage;
    successAlert.style.display = 'block';
    messageContainer.style.display = 'block';
    
    console.log('Success message displayed:', successMessage);
    console.log('Alert display:', successAlert.style.display);
    console.log('Container display:', messageContainer.style.display);
    
    // Make success message accessible
    successAlert.focus();
    
    // Scroll to the top to ensure the message is visible
    messageContainer.scrollIntoView({ behavior: 'smooth', block: 'start' });

    // Handle redirect if needed
    const redirectUrl = form.getAttribute('data-redirect-url');
    const redirectDelay = parseInt(form.getAttribute('data-redirect-delay') || '0', 10) * 1000;

    if (redirectUrl) {
      // Redirect information is available through aria-live regions in the form

      setTimeout(() => {
        window.location.href = redirectUrl;
      }, redirectDelay || 0);
    }

    // Reset the form but keep it visible
    form.reset();
    form.classList.remove('was-validated');
    
    // Remove any validation states from fields
    const fields = form.querySelectorAll('.is-valid, .is-invalid');
    fields.forEach(field => {
      field.classList.remove('is-valid', 'is-invalid');
    });
    
    // Dispatch form reset event
    this.dispatchEvent(form, 'easya11y:form:reset', {
      form
    });
  }

  /**
   * Handle form submission error
   * @param {HTMLFormElement} form - The form element
   * @param {Error} error - The error object
   */
  handleFormError(form, error) {
    // Dispatch error event
    this.dispatchEvent(form, 'easya11y:form:error', {
      form,
      error: {
        message: error.message,
        stack: error.stack
      }
    });
    
    // Show error message
    const errorMessage = form.getAttribute('data-error-message') || 'There was a problem submitting the form. Please try again.';
    console.error('Form submission error:', error);
    
    // Look for existing message container or create one if it doesn't exist
    let messageContainer = form.querySelector('.form-messages');
    let errorAlert = form.querySelector('.alert-danger');
    
    // If the message container doesn't exist, create it
    if (!messageContainer) {
      messageContainer = document.createElement('div');
      messageContainer.className = 'form-messages';
      // Insert at the beginning of the form
      form.insertBefore(messageContainer, form.firstChild);
    }
    
    // If the error alert doesn't exist, create it
    if (!errorAlert) {
      errorAlert = document.createElement('div');
      errorAlert.className = 'alert alert-danger';
      errorAlert.setAttribute('role', 'alert');
      errorAlert.setAttribute('tabindex', '-1');
      messageContainer.appendChild(errorAlert);
    }
    
    // Update and display the error message
    errorAlert.textContent = errorMessage;
    errorAlert.style.display = 'block';
    messageContainer.style.display = 'block';
    
    // Make error message accessible
    errorAlert.focus();
    
    // Scroll to the top to ensure the message is visible
    messageContainer.scrollIntoView({ behavior: 'smooth', block: 'start' });
    
    // Hide the validation summary if it exists - we only want to show the error alert
    const validationSummary = form.querySelector('.validation-summary');
    if (validationSummary) {
      validationSummary.style.display = 'none';
    }
  }

  /**
   * Set up conditional fields
   * @param {HTMLFormElement} form - The form containing conditional fields
   */
  setupConditionalFields(form) {
    const conditionalContainers = form.querySelectorAll('[data-form-conditional]');

    if (!conditionalContainers.length) {
      return;
    }

    // Initial evaluation of all conditionals
    conditionalContainers.forEach(container => {
      this.evaluateConditional(container, form);
    });

    // Set up change listeners for all form fields
    const inputs = form.querySelectorAll('input, select, textarea');
    inputs.forEach(input => {
      input.addEventListener('change', () => {
        // Re-evaluate all conditionals when any field changes
        conditionalContainers.forEach(container => {
          this.evaluateConditional(container, form);
        });
      });
    });
  }

  /**
   * Evaluate a conditional container
   * @param {HTMLElement} container - The conditional container
   * @param {HTMLFormElement} form - The parent form
   */
  evaluateConditional(container, form) {
    // The form-conditional__info visibility is handled by CSS classes
    // No need to set inline styles here
    const infoDiv = container.querySelector('.form-conditional__info');
    if (infoDiv) {
      // Only set aria-hidden based on author mode, let CSS handle display
      if (this.isAuthorMode) {
        infoDiv.setAttribute('aria-hidden', 'false');
      } else {
        infoDiv.setAttribute('aria-hidden', 'true');
      }
    }
    
    const fieldName = container.getAttribute('data-field-to-check');
    const valueToMatch = container.getAttribute('data-value-to-match');
    const operator = container.getAttribute('data-operator') || 'equals';

    // Check if we're in author mode - always show conditional fields in author mode
    if (this.isAuthorMode) {
      // Show the container in author mode
      container.style.display = '';
      container.setAttribute('aria-hidden', 'false');

      // Enable all form fields within the container
      const nestedInputs = container.querySelectorAll('input, select, textarea');
      nestedInputs.forEach(input => {
        input.disabled = false;
        input.setAttribute('aria-hidden', 'false');
      });
      return;
    }

    if (!fieldName) {
      return;
    }

    // Find the field to check
    const field = form.querySelector(`[name="${fieldName}"]`);
    if (!field) {
      return;
    }

    let fieldValue = field.value;

    // Handle radio buttons and checkboxes
    if (field.type === 'radio' || field.type === 'checkbox') {
      const checkedField = form.querySelector(`[name="${fieldName}"]:checked`);
      fieldValue = checkedField ? checkedField.value : '';
    }

    // Evaluate the condition based on the operator
    let conditionMet = false;

    switch (operator) {
      case 'equals':
        conditionMet = fieldValue === valueToMatch;
        break;
      case 'notEquals':
        conditionMet = fieldValue !== valueToMatch;
        break;
      case 'contains':
        conditionMet = fieldValue.includes(valueToMatch);
        break;
      case 'notContains':
        conditionMet = !fieldValue.includes(valueToMatch);
        break;
      case 'greaterThan':
        conditionMet = parseFloat(fieldValue) > parseFloat(valueToMatch);
        break;
      case 'lessThan':
        conditionMet = parseFloat(fieldValue) < parseFloat(valueToMatch);
        break;
      default:
        conditionMet = fieldValue === valueToMatch;
    }

    // Dispatch evaluation event
    this.dispatchEvent(form, 'easya11y:conditional:evaluated', {
      form,
      container,
      fieldName,
      condition: {
        field: fieldName,
        operator,
        value: valueToMatch,
        met: conditionMet
      }
    });

    // Show or hide the container based on the condition
    if (conditionMet) {
      container.style.display = '';
      container.setAttribute('aria-hidden', 'false');

      // Enable all form fields within the container
      const nestedInputs = container.querySelectorAll('input, select, textarea');
      nestedInputs.forEach(input => {
        input.disabled = false;
        input.setAttribute('aria-hidden', 'false');
      });
      
      // Dispatch shown event
      this.dispatchEvent(form, 'easya11y:conditional:shown', {
        form,
        container,
        fieldName,
        condition: {
          field: fieldName,
          operator,
          value: valueToMatch
        }
      });
    } else {
      container.style.display = 'none';
      container.setAttribute('aria-hidden', 'true');

      // Disable all form fields within the container
      const nestedInputs = container.querySelectorAll('input, select, textarea');
      nestedInputs.forEach(input => {
        input.disabled = true;
        input.setAttribute('aria-hidden', 'true');
      });
      
      // Dispatch hidden event
      this.dispatchEvent(form, 'easya11y:conditional:hidden', {
        form,
        container,
        fieldName,
        condition: {
          field: fieldName,
          operator,
          value: valueToMatch
        }
      });
    }
  }

  /**
   * Set up accessibility features for the form
   * @param {HTMLFormElement} form - The form to enhance
   */
  setupAccessibility(form) {
    // Add required field indicators for screen readers
    const requiredFields = form.querySelectorAll('[required]');
    requiredFields.forEach(field => {
      const label = this.findLabelForField(field);
      if (label) {
        // Don't modify if already has the required marker
        if (!label.querySelector('.sr-only')) {
          // Add screen reader text
          const srSpan = document.createElement('span');
          srSpan.classList.add('sr-only');
          srSpan.textContent = '(required)';
          label.appendChild(srSpan);

          // If the label doesn't already have a required visual marker
          if (!label.querySelector('.required-marker')) {
            const requiredMarker = document.createElement('span');
            requiredMarker.classList.add('required-marker');
            requiredMarker.setAttribute('aria-hidden', 'true');
            requiredMarker.textContent = '*';
            label.appendChild(requiredMarker);
          }
        }
      }
    });

    // Add keyboard navigation
    this.setupKeyboardNavigation(form);

    // Add focus management
    this.setupFocusManagement(form);
  }

  /**
   * Find the label element for a form field
   * @param {HTMLElement} field - The form field
   * @returns {HTMLElement|null} The label element or null if not found
   */
  findLabelForField(field) {
    if (!field.id) return null;

    // Try to find explicit label
    let label = document.querySelector(`label[for="${field.id}"]`);

    // If no explicit label, check for implicit (parent) label
    if (!label) {
      let parent = field.parentElement;
      while (parent) {
        if (parent.tagName === 'LABEL') {
          label = parent;
          break;
        }
        parent = parent.parentElement;
      }
    }

    return label;
  }

  /**
   * Set up keyboard navigation
   * @param {HTMLFormElement} form - The form to enhance
   */
  setupKeyboardNavigation(form) {
    // Handle Enter key on non-submit buttons
    const buttons = form.querySelectorAll('button:not([type="submit"])');
    buttons.forEach(button => {
      button.addEventListener('keydown', event => {
        if (event.key === 'Enter') {
          event.preventDefault();
          button.click();
        }
      });
    });

    // Improve keyboard navigation for nested inputs
    const formGroups = form.querySelectorAll('.form-group');
    formGroups.forEach(group => {
      const input = group.querySelector('input, select, textarea');
      const label = group.querySelector('label');

      if (input && label) {
        // Ensure input has an ID
        if (!input.id) {
          input.id = `form-field-${Date.now()}-${Math.floor(Math.random() * 1000)}`;
        }

        // Associate label with input
        label.setAttribute('for', input.id);
      }
    });
  }

  /**
   * Set up focus management
   * @param {HTMLFormElement} form - The form to enhance
   */
  setupFocusManagement(form) {
    // Create a focus trap for modals or popups
    const formId = form.id || `form-${Date.now()}`;

    // Add appropriate tabindex to interactive elements
    const interactiveElements = form.querySelectorAll('button, input, select, textarea, a');
    interactiveElements.forEach(element => {
      if (!element.hasAttribute('tabindex')) {
        element.setAttribute('tabindex', '0');
      }
    });

    // Non-interactive elements that need focus
    const focusableNonInteractive = form.querySelectorAll('.alert');
    focusableNonInteractive.forEach(element => {
      if (!element.hasAttribute('tabindex')) {
        element.setAttribute('tabindex', '-1');
      }
    });
  }

  /**
   * This method has been removed as we're relying on native ARIA attributes
   * for accessibility instead of JavaScript announcements
   */
}

// Initialize when the DOM is fully loaded
document.addEventListener('DOMContentLoaded', () => {
  console.log('DOM loaded, initializing FormHandler');
  if(!document.body.classList.contains('easya11y-init')) {
    new FormHandler();
  }
});

// Also try to catch forms that might be added after initial load
function setupMutationObserver() {
  if (!document.body) {
    // Body doesn't exist yet, wait for it
    setTimeout(setupMutationObserver, 100);
    return;
  }

  const observer = new MutationObserver((mutations) => {
    let shouldReinitialize = false;
    mutations.forEach((mutation) => {
      if (mutation.type === 'childList') {
        mutation.addedNodes.forEach((node) => {
          if (node.nodeType === Node.ELEMENT_NODE) {
            if (node.tagName === 'FORM' || node.querySelector('form')) {
              shouldReinitialize = true;
            }
          }
        });
      }
    });
    
    if (shouldReinitialize && !document.body.classList.contains('easya11y-init')) {
      console.log('New forms detected, reinitializing FormHandler');
      new FormHandler();
    }
  });

  // Start observing
  observer.observe(document.body, {
    childList: true,
    subtree: true
  });
}

// Setup the observer when DOM is ready
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', setupMutationObserver);
} else {
  setupMutationObserver();
}

export default FormHandler;