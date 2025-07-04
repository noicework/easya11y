[#-- render form macros --]

[#macro renderForm form contentId formResultsLink]

    [#if form?has_content]

        [#local questionCount = 0]

        [#if form.title?has_content]
            <h1>${form.title!}</h1>
        [/#if]
        [#if form.description?has_content]
            <p>${form.description!}</p>
        [/#if]

        <form id="form-${contentId}" action="${ctx.contextPath}/.rest/forms/v1/forms/${form.id!}">

            [#if form.sections?has_content && form.sections?size > 0]
                [#list form.sections?sort_by("orderIndex") as section]
                    <h4>${section.title!}</h4><br/><br/>
                    [@renderQustions section.questions questionCount /]

                    [#local questionCount = questionCount + section.questions?size]
                [/#list]
            [/#if]

            [#if form.questions?has_content && form.questions?size > 0]
                <br/><br/>
                [@renderQustions form.questions questionCount  /]
                [#local questionCount = questionCount + form.questions?size]
            [/#if]

            <br/>
            <input type="submit" value="Submit"/>
            <input type="hidden" id="questionsCount-${contentId}" name="questionsCount-${contentId}" value="${questionCount}">
            <br/><br/>
            <p class="message success" id="success-${contentId}" style="display: none;">
                Data has been successfully submitted.
                [#if formResultsLink?has_content]
                    You can view form results <a href="${formResultsLink + "?form=" + form.id!}">here.</a>
                [/#if]
            </p>
            <p class="message error" id="error-${contentId}" style="display: none;">
                There was a problem submitting the data.
            </p>
        </form>

        [@renderScript contentId /]

    [#else]
        <p>No form found. Please pass proper form id.</p>
    [/#if]

[/#macro]

[#-- macros, functions part --]
[#macro renderQustions questions renderedQuestionsCount]
    [#if questions?has_content && questions?size > 0]
        [#local questionIndex = renderedQuestionsCount]
        [#list questions?sort_by("orderIndex") as question]
            [#-- assign --]
            [#local questionIndex = questionIndex + 1]
            [#local questionValue= "response-" + questionIndex + ".value" ]
            [#local questionId= "response-" + questionIndex + ".questionId" ]
            [#-- render --]
            [@renderQustion question questionId questionValue /]
        [/#list]
    [/#if]
[/#macro]

[#macro renderQustion question questionId questionValue]
    [#if question?has_content]
        <input type="hidden" name="${questionId}" value="${question.id!}">
        [#if "text" == question.questionType]
            [@renderFreeText question questionValue /]
        [#elseif "single" == question.questionType]
            [@renderSingleChoice question questionValue /]
        [#elseif "multi" == question.questionType]
            [@renderMultiChoice question questionValue /]
        [#elseif "range" == question.questionType]
            [@renderRange question questionValue /]
        [/#if]
        <br/><br/>
    [/#if]
[/#macro]

[#macro renderRange question questionValue]
    [#local questionId = "q_" + question.id + ""]
    <label for="${questionId}">${question.question!}</label><br/>
    <input id="${questionId}" type="range" min="${(question.rangeFrom?has_content)?then(question.rangeFrom?c,0)}" max="${(question.rangeTo?has_content)?then(question.rangeTo?c,0)}" step="1" name="${questionValue}"><br/>
[/#macro]

[#macro renderFreeText question questionValue]
    [#local questionId = "q_" + question.id + ""]
    <label for="${questionId}">${question.question!}</label><br/>
    <input id="${questionId}" type="text" name="${questionValue}"><br/>
[/#macro]

[#macro renderSingleChoice question questionValue]
    [#if question.answerOptions?has_content && question.answerOptions?size>0]
        <label>${question.question!}</label><br/>
        <input type="hidden" id="${questionValue}-count" value="1">
        [#list question.answerOptions?sort_by("orderIndex") as answerOption]
            [#local questionId = "q_" + question.id + "_" + answerOption.id]
            <input id="${questionId}" type="radio" name="${questionValue}-0" data-name="${questionValue}" value="${answerOption.value!}">
            <label for="${questionId}">${answerOption.label!}</label>
            [#if answerOption.freeText == "showFreeText"]
                <label for="${questionId}-free-text" style="display: none;" name="${questionValue}-free-text-label">${answerOption.freeTextLabel!}</label>
                <input id="${questionId}-free-text" style="display: none;" disabled="disabled" type="text" name="${questionValue}-free-text-0">
            [/#if]
            <br/>
        [/#list]
    [/#if]
    <script type="application/javascript">
      var radios = document.querySelectorAll('input[type=radio][name="${questionValue}-0"]');

      function changeHandler() {

        var elId = this.id;
        var elName = this.getAttribute('data-name');

        document.querySelectorAll("input[name='" + elName + "-free-text-0']").forEach(el => {
          el.style.display = "none";
          el.value = '';
          el.setAttribute('disabled', 'disabled');
        });
        document.querySelectorAll("label[name='" + elName + "-free-text-label']").forEach(el => {
          el.style.display = "none";
        });

        document.querySelectorAll("input[id='" + elId + "-free-text']").forEach(el => {
          el.style.display = "inline";
          el.removeAttribute('disabled');
        });
        document.querySelectorAll("label[for='" + elId + "-free-text']").forEach(el => {
          el.style.display = "inline";
        });
      }

      Array.prototype.forEach.call(radios, function(radio) {
        radio.addEventListener('change', changeHandler);
      });
    </script>
[/#macro]

[#macro renderMultiChoice question questionValue]
    [#if question.answerOptions?has_content && question.answerOptions?size>0]
        <label>${question.question!}</label><br/>
        <input type="hidden" id="${questionValue}-count" value="${question.answerOptions?size!}">
        [#list question.answerOptions?sort_by("orderIndex") as answerOption]
            [#local questionId = "q_" + question.id + "_" + answerOption.id]
            <input id="${questionId}" type="checkbox" name="${questionValue}-${answerOption_index}" value="${answerOption.value!}">
            <label for="${questionId}">${answerOption.label!}</label>
            [#if answerOption.freeText == "showFreeText"]
                <label for="${questionId}-free-text">${answerOption.freeTextLabel!}</label>
                <input id="${questionId}-free-text" type="text" name="${questionValue}-free-text-${answerOption_index}">
            [/#if]
            <br/>
        [/#list]
    [/#if]
[/#macro]

[#macro renderScript contentId]
[#-- js part --]
<script type="application/javascript">

  var form = document.getElementById("form-${contentId}");

  form.onsubmit = function () {

    debugger;

    var elements = document.getElementsByClassName('message');
    for (var k = 0; k < elements.length; k++) {
      elements[k].style.display = 'none';
    }

    var formData = new FormData(form);
    var questionsCount = document.getElementById("questionsCount-${contentId}").value;
    var data = [];
    for (var i = 1; i <= questionsCount; i++) {
      var questionId = formData.get('response-' + i + '.questionId');
      var optionsCount = null;
      var options = document.getElementById('response-' + i + '.value-count');
      if (options) {
        optionsCount = options.value;
      }
      if (optionsCount) {
        for (var k = 0; k < optionsCount; k++) {
          var value = formData.get('response-' + i + '.value-' + k);
          if (value) {
            var freeText = formData.get('response-' + i + '.value-free-text-' + k);

            if (freeText && freeText !== '') {
              data.push({
                'questionId': questionId,
                'value': value,
                'freeTextValue': freeText
              })
            } else {
              data.push({
                'questionId': questionId,
                'value': value
              })
            }

          }
        }
      } else {
        var values = formData.getAll('response-' + i + '.value');
        for (var j = 0; j < values.length; j++) {
          data.push({
            'questionId': questionId,
            'value': values[j]
          })
        }
      }
    }
    fetch(form.action, {
      method: 'post',
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(data)
    }).then(function (response) {
      // The API call was successful!
      if(!response.ok) {
        throw new Error(response.status);
      }
      return response.json();
    }).then(function (data) {
      // This is the JSON from our response
      //console.log(data);
      document.getElementById("success-${contentId}").style.display = 'inline';
    }).catch(function (err) {
      // There was an error
      //console.warn('Something went wrong.', err);
      document.getElementById("error-${contentId}").style.display = 'inline';
    });

    return false;
  }

</script>
[/#macro]
