#Rest Api

##Api path: /.rest/forms/v1

| Method       | Path     | Descritpion     |
| :------------- | :---------- | :----------- |
| [Get](#example-get-forms) | /forms   | Returns a list of all forms defined in the system.    |
| [Get](#example-get-form) | /forms/{id}   | Returns details of a given form. (Returns whole form structure.)    |
| [Post](#example-post-response) | /forms/{id}   | Post the form response contained in the body (json representation of the form response.)    |
| [Get](#example-get-results) | /forms/{id}/results   | Returns a list of all form responses (results).    |

## Post form response

Method signature: 
```
Response response(final @PathParam("id") long id, final List<ResponseItemDto> formResponseItemDtoList)
```
Method body (list of):

```
class ResponseItemDto {

    String value;
    long questionId;
}
```

## Examples


### Example get forms

<details>
    <summary>
    Request:
    </summary>

    ```
    GET {{host}}{{base_path}}/forms/
    Authorization: Basic {{username}} {{password}}
    Content-Type: application/json
    
    ```
</details>
<br/>
<details>
    <summary>
    Response:
    </summary>

    ```
    [
      {
        "id": 1,
        "version": 2,
        "created": 1618675260213,
        "createdBy": "superuser",
        "modified": 1619780433908,
        "modifiedBy": "superuser",
        "title": "Example form",
        "description": "Form with different types of questions.\nPlease answer the questions bellow!",
        "sections": [],
        "questions": [],
        "anonymize": true,
        "onlyAuthenticated": false,
        "publicResults": false,
        "localisations": []
      },
      {
        "id": 2,
        "version": 1,
        "created": 1619781967725,
        "createdBy": "superuser",
        "modified": 1619781967725,
        "modifiedBy": "superuser",
        "title": "Example form video",
        "description": "Example form video",
        "sections": [],
        "questions": [],
        "anonymize": false,
        "onlyAuthenticated": false,
        "publicResults": false,
        "localisations": []
      },
      {
        "id": 5,
        "version": 1,
        "created": 1620392912710,
        "createdBy": "superuser",
        "modified": 1620392912710,
        "modifiedBy": "superuser",
        "title": "My form title",
        "description": "My form description",
        "sections": [],
        "questions": [],
        "anonymize": false,
        "onlyAuthenticated": false,
        "publicResults": false,
        "localisations": []
      }
    ]
    ```
</details>

### Example get form

<details>
    <summary>
    Request:
    </summary>

    ```
    GET {{host}}{{base_path}}/forms/1
    Authorization: Basic {{username}} {{password}}
    Content-Type: application/json
    
    ###
    ```
</details>
<br/>
<details>
    <summary>
    Response:
    </summary>
    
    ```
    {
      "id": 1,
      "version": 2,
      "created": 1618675260213,
      "createdBy": "superuser",
      "modified": 1619780433908,
      "modifiedBy": "superuser",
      "title": "Example form",
      "description": "Form with different types of questions.\nPlease answer the questions bellow!",
      "sections": [
        {
          "id": 1,
          "version": 2,
          "created": 1618675326562,
          "createdBy": "superuser",
          "modified": 1619780045838,
          "modifiedBy": "superuser",
          "title": "First set of questions",
          "description": "First set of questions",
          "questions": [
            {
              "id": 1,
              "version": 3,
              "created": 1618691256819,
              "createdBy": "superuser",
              "modified": 1619782468197,
              "modifiedBy": "superuser",
              "title": "Early bird",
              "question": "Are you an early bird or a night owl?",
              "questionType": "single",
              "answerOptions": [
                {
                  "id": 1,
                  "version": 1,
                  "created": 1618691829838,
                  "createdBy": "superuser",
                  "modified": 1618691829838,
                  "modifiedBy": "superuser",
                  "title": "Early bird",
                  "label": "Early bird",
                  "value": "early-bird",
                  "questionId": 1,
                  "localisations": []
                },
                {
                  "id": 2,
                  "version": 1,
                  "created": 1618691860965,
                  "createdBy": "superuser",
                  "modified": 1618691860965,
                  "modifiedBy": "superuser",
                  "title": "Night owl",
                  "label": "Night owl",
                  "value": "night-owl",
                  "questionId": 1,
                  "localisations": []
                }
              ],
              "sectionId": 1,
              "formId": null,
              "localisations": []
            },
            {
              "id": 2,
              "version": 2,
              "created": 1618691896397,
              "createdBy": "superuser",
              "modified": 1618693606565,
              "modifiedBy": "superuser",
              "title": "Superpower",
              "question": "Which superpower would you like to have?",
              "questionType": "single",
              "answerOptions": [
                {
                  "id": 5,
                  "version": 1,
                  "created": 1618691956791,
                  "createdBy": "superuser",
                  "modified": 1618691956791,
                  "modifiedBy": "superuser",
                  "title": "Teleportation",
                  "label": "Teleportation",
                  "value": "teleportation",
                  "questionId": 2,
                  "localisations": []
                },
                {
                  "id": 4,
                  "version": 1,
                  "created": 1618691939624,
                  "createdBy": "superuser",
                  "modified": 1618691939624,
                  "modifiedBy": "superuser",
                  "title": "Invisibility",
                  "label": "Invisibility",
                  "value": "invisibility",
                  "questionId": 2,
                  "localisations": []
                },
                {
                  "id": 3,
                  "version": 1,
                  "created": 1618691922556,
                  "createdBy": "superuser",
                  "modified": 1618691922556,
                  "modifiedBy": "superuser",
                  "title": "Mind reading",
                  "label": "Mind reading",
                  "value": "mind-reading",
                  "questionId": 2,
                  "localisations": []
                },
                {
                  "id": 6,
                  "version": 1,
                  "created": 1618691973887,
                  "createdBy": "superuser",
                  "modified": 1618691973887,
                  "modifiedBy": "superuser",
                  "title": "Flying",
                  "label": "Flying",
                  "value": "flying",
                  "questionId": 2,
                  "localisations": []
                }
              ],
              "sectionId": 1,
              "formId": null,
              "localisations": []
            },
            {
              "id": 3,
              "version": 1,
              "created": 1618692599599,
              "createdBy": "superuser",
              "modified": 1618692599599,
              "modifiedBy": "superuser",
              "title": "Project priority",
              "question": "Which projects should we prioritize this quarter?",
              "questionType": "multi",
              "answerOptions": [
                {
                  "id": 21,
                  "version": 1,
                  "created": 1618692707856,
                  "createdBy": "superuser",
                  "modified": 1618692707856,
                  "modifiedBy": "superuser",
                  "title": "WTF",
                  "label": "WTF",
                  "value": "WTF",
                  "questionId": 3,
                  "localisations": []
                },
                {
                  "id": 19,
                  "version": 1,
                  "created": 1618692663958,
                  "createdBy": "superuser",
                  "modified": 1618692663958,
                  "modifiedBy": "superuser",
                  "title": "MMF",
                  "label": "MMF",
                  "value": "MMF",
                  "questionId": 3,
                  "localisations": []
                },
                {
                  "id": 18,
                  "version": 1,
                  "created": 1618692653119,
                  "createdBy": "superuser",
                  "modified": 1618692653119,
                  "modifiedBy": "superuser",
                  "title": "ASM",
                  "label": "ASM",
                  "value": "ASM",
                  "questionId": 3,
                  "localisations": []
                },
                {
                  "id": 20,
                  "version": 1,
                  "created": 1618692688727,
                  "createdBy": "superuser",
                  "modified": 1618692688727,
                  "modifiedBy": "superuser",
                  "title": "SOS",
                  "label": "SOS",
                  "value": "SOS",
                  "questionId": 3,
                  "localisations": []
                }
              ],
              "sectionId": 1,
              "formId": null,
              "localisations": []
            },
            {
              "id": 4,
              "version": 1,
              "created": 1618692866139,
              "createdBy": "superuser",
              "modified": 1618692866139,
              "modifiedBy": "superuser",
              "title": "Responsible for L&D",
              "question": "Who should be responsible for L&D?",
              "questionType": "multi",
              "answerOptions": [
                {
                  "id": 22,
                  "version": 1,
                  "created": 1618692882450,
                  "createdBy": "superuser",
                  "modified": 1618692882450,
                  "modifiedBy": "superuser",
                  "title": "Individual",
                  "label": "Individual",
                  "value": "individual",
                  "questionId": 4,
                  "localisations": []
                },
                {
                  "id": 23,
                  "version": 1,
                  "created": 1618692912297,
                  "createdBy": "superuser",
                  "modified": 1618692912297,
                  "modifiedBy": "superuser",
                  "title": "Team lead",
                  "label": "Team lead",
                  "value": "team-lead",
                  "questionId": 4,
                  "localisations": []
                },
                {
                  "id": 24,
                  "version": 1,
                  "created": 1618692927313,
                  "createdBy": "superuser",
                  "modified": 1618692927313,
                  "modifiedBy": "superuser",
                  "title": "HR",
                  "label": "HR",
                  "value": "hr",
                  "questionId": 4,
                  "localisations": []
                }
              ],
              "sectionId": 1,
              "formId": null,
              "localisations": []
            },
            {
              "id": 5,
              "version": 2,
              "created": 1618675347609,
              "createdBy": "superuser",
              "modified": 1618693556267,
              "modifiedBy": "superuser",
              "title": "State of mind",
              "question": "Using one word, what’s your state of mind right now?",
              "questionType": "text",
              "answerOptions": [],
              "sectionId": 1,
              "formId": null,
              "localisations": []
            }
          ],
          "formId": 1,
          "localisations": []
        },
        {
          "id": 2,
          "version": 3,
          "created": 1618691198937,
          "createdBy": "superuser",
          "modified": 1619780059394,
          "modifiedBy": "superuser",
          "title": "Second set of questions",
          "description": "Second set of questions",
          "questions": [
            {
              "id": 6,
              "version": 1,
              "created": 1618692022455,
              "createdBy": "superuser",
              "modified": 1618692022455,
              "modifiedBy": "superuser",
              "title": "Time-travel",
              "question": "If you could time-travel, which period would you go to?",
              "questionType": "single",
              "answerOptions": [
                {
                  "id": 7,
                  "version": 1,
                  "created": 1618692038109,
                  "createdBy": "superuser",
                  "modified": 1618692038109,
                  "modifiedBy": "superuser",
                  "title": "The past",
                  "label": "The past",
                  "value": "past",
                  "questionId": 6,
                  "localisations": []
                },
                {
                  "id": 8,
                  "version": 1,
                  "created": 1618692056474,
                  "createdBy": "superuser",
                  "modified": 1618692056474,
                  "modifiedBy": "superuser",
                  "title": "The future",
                  "label": "The future",
                  "value": "future",
                  "questionId": 6,
                  "localisations": []
                },
                {
                  "id": 9,
                  "version": 1,
                  "created": 1618692076291,
                  "createdBy": "superuser",
                  "modified": 1618692076291,
                  "modifiedBy": "superuser",
                  "title": "I’m good where I am",
                  "label": "I’m good where I am",
                  "value": "present",
                  "questionId": 6,
                  "localisations": []
                }
              ],
              "sectionId": 2,
              "formId": null,
              "localisations": []
            },
            {
              "id": 9,
              "version": 1,
              "created": 1618693877916,
              "createdBy": "superuser",
              "modified": 1618693877916,
              "modifiedBy": "superuser",
              "title": "State of mind",
              "question": "If age is only a state of mind, which category best describes your state of mind right now?",
              "questionType": "multi",
              "answerOptions": [
                {
                  "id": 35,
                  "version": 1,
                  "created": 1618693931380,
                  "createdBy": "superuser",
                  "modified": 1618693931380,
                  "modifiedBy": "superuser",
                  "title": "Tormented teenager",
                  "label": "Tormented teenager",
                  "value": "teenager",
                  "questionId": 9,
                  "localisations": []
                },
                {
                  "id": 37,
                  "version": 1,
                  "created": 1618693972428,
                  "createdBy": "superuser",
                  "modified": 1618693972428,
                  "modifiedBy": "superuser",
                  "title": "Groovy grandparent",
                  "label": "Groovy grandparent",
                  "value": "grandparent",
                  "questionId": 9,
                  "localisations": []
                },
                {
                  "id": 36,
                  "version": 1,
                  "created": 1618693950612,
                  "createdBy": "superuser",
                  "modified": 1618693950612,
                  "modifiedBy": "superuser",
                  "title": "Mad mid-lifer",
                  "label": "Mad mid-lifer",
                  "value": "mid-lifer",
                  "questionId": 9,
                  "localisations": []
                },
                {
                  "id": 34,
                  "version": 1,
                  "created": 1618693913307,
                  "createdBy": "superuser",
                  "modified": 1618693913307,
                  "modifiedBy": "superuser",
                  "title": "Cheeky child",
                  "label": "Cheeky child",
                  "value": "child",
                  "questionId": 9,
                  "localisations": []
                }
              ],
              "sectionId": 2,
              "formId": null,
              "localisations": []
            },
            {
              "id": 8,
              "version": 2,
              "created": 1618693390509,
              "createdBy": "superuser",
              "modified": 1618693618695,
              "modifiedBy": "superuser",
              "title": "Strangest thing",
              "question": "What’s the strangest thing you did while attending a meeting online?",
              "questionType": "multi",
              "answerOptions": [
                {
                  "id": 30,
                  "version": 1,
                  "created": 1618693683957,
                  "createdBy": "superuser",
                  "modified": 1618693683957,
                  "modifiedBy": "superuser",
                  "title": "Wore pajamas",
                  "label": "Wore pajamas",
                  "value": "pajamas",
                  "questionId": 8,
                  "localisations": []
                },
                {
                  "id": 32,
                  "version": 1,
                  "created": 1618693739161,
                  "createdBy": "superuser",
                  "modified": 1618693739161,
                  "modifiedBy": "superuser",
                  "title": "Watched Netflix",
                  "label": "Watched Netflix",
                  "value": "netflix",
                  "questionId": 8,
                  "localisations": []
                },
                {
                  "id": 33,
                  "version": 1,
                  "created": 1618693796591,
                  "createdBy": "superuser",
                  "modified": 1618693796591,
                  "modifiedBy": "superuser",
                  "title": "Other, but my lips are sealed",
                  "label": "Other, but my lips are sealed",
                  "value": "other",
                  "questionId": 8,
                  "localisations": []
                },
                {
                  "id": 29,
                  "version": 1,
                  "created": 1618693637057,
                  "createdBy": "superuser",
                  "modified": 1618693637057,
                  "modifiedBy": "superuser",
                  "title": "Ate breakfast",
                  "label": "Ate breakfast",
                  "value": "ate",
                  "questionId": 8,
                  "localisations": []
                },
                {
                  "id": 31,
                  "version": 1,
                  "created": 1618693710581,
                  "createdBy": "superuser",
                  "modified": 1618693710581,
                  "modifiedBy": "superuser",
                  "title": "Cooked lunch/dinner",
                  "label": "Cooked lunch/dinner",
                  "value": "cooked",
                  "questionId": 8,
                  "localisations": []
                }
              ],
              "sectionId": 2,
              "formId": null,
              "localisations": []
            },
            {
              "id": 7,
              "version": 1,
              "created": 1618692138162,
              "createdBy": "superuser",
              "modified": 1618692138162,
              "modifiedBy": "superuser",
              "title": "Multitask",
              "question": "Do you multitask when attending a meeting online?",
              "questionType": "single",
              "answerOptions": [
                {
                  "id": 10,
                  "version": 1,
                  "created": 1618692152745,
                  "createdBy": "superuser",
                  "modified": 1618692152745,
                  "modifiedBy": "superuser",
                  "title": "Yes, I’m guilty",
                  "label": "Yes, I’m guilty",
                  "value": "yes",
                  "questionId": 7,
                  "localisations": []
                },
                {
                  "id": 11,
                  "version": 1,
                  "created": 1618692177660,
                  "createdBy": "superuser",
                  "modified": 1618692177660,
                  "modifiedBy": "superuser",
                  "title": "My mind tends to wander",
                  "label": "My mind tends to wander",
                  "value": "wander",
                  "questionId": 7,
                  "localisations": []
                },
                {
                  "id": 12,
                  "version": 1,
                  "created": 1618692196977,
                  "createdBy": "superuser",
                  "modified": 1618692196977,
                  "modifiedBy": "superuser",
                  "title": "No, I’m 100% focused",
                  "label": "No, I’m 100% focused",
                  "value": "no",
                  "questionId": 7,
                  "localisations": []
                },
                {
                  "id": 13,
                  "version": 1,
                  "created": 1618692235707,
                  "createdBy": "superuser",
                  "modified": 1618692235707,
                  "modifiedBy": "superuser",
                  "title": "Sometimes",
                  "label": "Sometimes",
                  "value": "sometimes",
                  "questionId": 7,
                  "localisations": []
                }
              ],
              "sectionId": 2,
              "formId": null,
              "localisations": []
            },
            {
              "id": 10,
              "version": 1,
              "created": 1618675408727,
              "createdBy": "superuser",
              "modified": 1618693572523,
              "modifiedBy": "superuser",
              "title": "Inspires you",
              "question": "Which industry figure inspires you?",
              "questionType": "text",
              "answerOptions": [],
              "sectionId": 2,
              "formId": null,
              "localisations": []
            }
          ],
          "formId": 1,
          "localisations": []
        },
        {
          "id": 3,
          "version": 2,
          "created": 1618692533366,
          "createdBy": "superuser",
          "modified": 1619780071711,
          "modifiedBy": "superuser",
          "title": "Third set of questions",
          "description": "Third set of questions",
          "questions": [
            {
              "id": 13,
              "version": 2,
              "created": 1618675430914,
              "createdBy": "superuser",
              "modified": 1618693580000,
              "modifiedBy": "superuser",
              "title": "Role model",
              "question": "Who is your role model?",
              "questionType": "text",
              "answerOptions": [],
              "sectionId": 3,
              "formId": null,
              "localisations": []
            },
            {
              "id": 15,
              "version": 2,
              "created": 1618675489970,
              "createdBy": "superuser",
              "modified": 1618693587482,
              "modifiedBy": "superuser",
              "title": "Tech invention",
              "question": "What’s the best tech invention of the 21st Century?",
              "questionType": "text",
              "answerOptions": [],
              "sectionId": 3,
              "formId": null,
              "localisations": []
            },
            {
              "id": 11,
              "version": 1,
              "created": 1618692297159,
              "createdBy": "superuser",
              "modified": 1618692297159,
              "modifiedBy": "superuser",
              "title": "Hours online",
              "question": "How many hours a day do you spend online",
              "questionType": "single",
              "answerOptions": [
                {
                  "id": 15,
                  "version": 1,
                  "created": 1618692348815,
                  "createdBy": "superuser",
                  "modified": 1618692348815,
                  "modifiedBy": "superuser",
                  "title": "2-5 hours",
                  "label": "2-5 hours",
                  "value": "2-5",
                  "questionId": 11,
                  "localisations": []
                },
                {
                  "id": 14,
                  "version": 1,
                  "created": 1618692317599,
                  "createdBy": "superuser",
                  "modified": 1618692317599,
                  "modifiedBy": "superuser",
                  "title": "1-2 hours",
                  "label": "1-2 hours",
                  "value": "1-2",
                  "questionId": 11,
                  "localisations": []
                },
                {
                  "id": 16,
                  "version": 1,
                  "created": 1618692367830,
                  "createdBy": "superuser",
                  "modified": 1618692367830,
                  "modifiedBy": "superuser",
                  "title": "5-8 hours",
                  "label": "5-8 hours",
                  "value": "5-8",
                  "questionId": 11,
                  "localisations": []
                },
                {
                  "id": 17,
                  "version": 1,
                  "created": 1618692395726,
                  "createdBy": "superuser",
                  "modified": 1618692395726,
                  "modifiedBy": "superuser",
                  "title": "I lost count",
                  "label": "I lost count",
                  "value": "8>",
                  "questionId": 11,
                  "localisations": []
                }
              ],
              "sectionId": 3,
              "formId": null,
              "localisations": []
            },
            {
              "id": 12,
              "version": 1,
              "created": 1618692997239,
              "createdBy": "superuser",
              "modified": 1618692997239,
              "modifiedBy": "superuser",
              "title": "Improving areas",
              "question": "Which of these areas should we focus on improving?",
              "questionType": "multi",
              "answerOptions": [
                {
                  "id": 27,
                  "version": 1,
                  "created": 1618693316711,
                  "createdBy": "superuser",
                  "modified": 1618693316711,
                  "modifiedBy": "superuser",
                  "title": "Human resources",
                  "label": "Human resources",
                  "value": "hr",
                  "questionId": 12,
                  "localisations": []
                },
                {
                  "id": 25,
                  "version": 1,
                  "created": 1618693206145,
                  "createdBy": "superuser",
                  "modified": 1618693206145,
                  "modifiedBy": "superuser",
                  "title": "Project management",
                  "label": "Project management",
                  "value": "pm",
                  "questionId": 12,
                  "localisations": []
                },
                {
                  "id": 28,
                  "version": 1,
                  "created": 1618693358378,
                  "createdBy": "superuser",
                  "modified": 1618693358378,
                  "modifiedBy": "superuser",
                  "title": "Strategic planning",
                  "label": "Strategic planning",
                  "value": "plan",
                  "questionId": 12,
                  "localisations": []
                },
                {
                  "id": 26,
                  "version": 1,
                  "created": 1618693261556,
                  "createdBy": "superuser",
                  "modified": 1618693261556,
                  "modifiedBy": "superuser",
                  "title": "Technical development",
                  "label": "Technical development",
                  "value": "dev",
                  "questionId": 12,
                  "localisations": []
                }
              ],
              "sectionId": 3,
              "formId": null,
              "localisations": []
            },
            {
              "id": 14,
              "version": 2,
              "created": 1618675383428,
              "createdBy": "superuser",
              "modified": 1618693564795,
              "modifiedBy": "superuser",
              "title": "Grateful for",
              "question": "What are you most grateful for?",
              "questionType": "text",
              "answerOptions": [],
              "sectionId": 3,
              "formId": null,
              "localisations": []
            }
          ],
          "formId": 1,
          "localisations": []
        }
      ],
      "questions": [],
      "anonymize": true,
      "onlyAuthenticated": false,
      "publicResults": false,
      "localisations": []
    }
    ```
</details>

### Example post response

<details>
    <summary>
    Request:
    </summary>
    
    ```
    POST {{host}}{{base_path}}/forms/1
    Authorization: Basic {{username}} {{password}}
    Content-Type: application/json
    
    [
      {
        "questionId": 5,
        "value":"powerful"
      },
      {
        "questionId": 14,
        "value":"life"
      },
      {
        "questionId": 10,
        "value":"Nikola Tesla"
      },
      {
        "questionId": 13,
        "value":"father"
      },
      {
        "questionId": 15,
        "value":"bitcoin"
      },
      {
        "questionId": 1,
        "value":"night-owl"
      },
      {
        "questionId": 2,
        "value":"flying"
      },
      {
        "questionId": 6,
        "value":"future"
      },
      {
        "questionId": 7,
        "value":"no"
      },
      {
        "questionId": 11,
        "value":"8>"
      },
      {
        "questionId": 3,
        "value":"WTF"
      },
      {
        "questionId": 4,
        "value":"individual"
      },
      {
        "questionId": 12,
        "value":"dev"
      },
      {
        "questionId": 8,
        "value":"pajamas"
      },
      {
        "questionId": 9,
        "value":"grandparent"
      }
    ]
    
    ###
    ```
</details>
<br/>
<details>
    <summary>
    Response:
    </summary>
    
    ```
    {
      "id": 6,
      "version": 1,
      "created": 1620635015426,
      "createdBy": "anonymous",
      "modified": 1620635015426,
      "modifiedBy": "anonymous",
      "items": [
        {
          "id": 77,
          "version": 1,
          "created": 1620635015458,
          "createdBy": "anonymous",
          "modified": 1620635015458,
          "modifiedBy": "anonymous",
          "value": "8>",
          "questionId": 11
        },
        {
          "id": 78,
          "version": 1,
          "created": 1620635015471,
          "createdBy": "anonymous",
          "modified": 1620635015471,
          "modifiedBy": "anonymous",
          "value": "pajamas",
          "questionId": 8
        },
        {
          "id": 79,
          "version": 1,
          "created": 1620635015471,
          "createdBy": "anonymous",
          "modified": 1620635015471,
          "modifiedBy": "anonymous",
          "value": "flying",
          "questionId": 2
        },
        {
          "id": 80,
          "version": 1,
          "created": 1620635015471,
          "createdBy": "anonymous",
          "modified": 1620635015471,
          "modifiedBy": "anonymous",
          "value": "bitcoin",
          "questionId": 15
        },
        {
          "id": 81,
          "version": 1,
          "created": 1620635015471,
          "createdBy": "anonymous",
          "modified": 1620635015471,
          "modifiedBy": "anonymous",
          "value": "no",
          "questionId": 7
        },
        {
          "id": 82,
          "version": 1,
          "created": 1620635015471,
          "createdBy": "anonymous",
          "modified": 1620635015471,
          "modifiedBy": "anonymous",
          "value": "life",
          "questionId": 14
        },
        {
          "id": 83,
          "version": 1,
          "created": 1620635015471,
          "createdBy": "anonymous",
          "modified": 1620635015471,
          "modifiedBy": "anonymous",
          "value": "powerful",
          "questionId": 5
        },
        {
          "id": 84,
          "version": 1,
          "created": 1620635015471,
          "createdBy": "anonymous",
          "modified": 1620635015471,
          "modifiedBy": "anonymous",
          "value": "dev",
          "questionId": 12
        },
        {
          "id": 85,
          "version": 1,
          "created": 1620635015471,
          "createdBy": "anonymous",
          "modified": 1620635015471,
          "modifiedBy": "anonymous",
          "value": "Nikola Tesla",
          "questionId": 10
        },
        {
          "id": 86,
          "version": 1,
          "created": 1620635015471,
          "createdBy": "anonymous",
          "modified": 1620635015471,
          "modifiedBy": "anonymous",
          "value": "grandparent",
          "questionId": 9
        },
        {
          "id": 87,
          "version": 1,
          "created": 1620635015471,
          "createdBy": "anonymous",
          "modified": 1620635015471,
          "modifiedBy": "anonymous",
          "value": "father",
          "questionId": 13
        },
        {
          "id": 88,
          "version": 1,
          "created": 1620635015471,
          "createdBy": "anonymous",
          "modified": 1620635015471,
          "modifiedBy": "anonymous",
          "value": "WTF",
          "questionId": 3
        },
        {
          "id": 89,
          "version": 1,
          "created": 1620635015471,
          "createdBy": "anonymous",
          "modified": 1620635015471,
          "modifiedBy": "anonymous",
          "value": "individual",
          "questionId": 4
        },
        {
          "id": 90,
          "version": 1,
          "created": 1620635015471,
          "createdBy": "anonymous",
          "modified": 1620635015471,
          "modifiedBy": "anonymous",
          "value": "future",
          "questionId": 6
        },
        {
          "id": 91,
          "version": 1,
          "created": 1620635015471,
          "createdBy": "anonymous",
          "modified": 1620635015471,
          "modifiedBy": "anonymous",
          "value": "night-owl",
          "questionId": 1
        }
      ],
      "formId": 1
    }
    ```
</details>

### Example get results
<details>
    <summary>
    Request:
    </summary>
    
    ```
    GET {{host}}{{base_path}}/forms/1/results
    Authorization: Basic {{username}} {{password}}
    Content-Type: application/json
    
    ###
    ```
</details>
<br/>
<details>
    <summary>
    Response:
    </summary>
    
    ```
    [
      {
        "id": 1,
        "version": 1,
        "created": 1619607498255,
        "createdBy": "superuser",
        "modified": 1619607498255,
        "modifiedBy": "superuser",
        "items": [
          {
            "id": 1,
            "version": 1,
            "created": 1619780298274,
            "createdBy": "superuser",
            "modified": 1619780298274,
            "modifiedBy": "superuser",
            "value": "Nikola Tesla",
            "questionId": 10
          },
          {
            "id": 2,
            "version": 1,
            "created": 1619780298285,
            "createdBy": "superuser",
            "modified": 1619780298285,
            "modifiedBy": "superuser",
            "value": "pajamas",
            "questionId": 8
          },
          {
            "id": 3,
            "version": 1,
            "created": 1619607498255,
            "createdBy": "superuser",
            "modified": 1619607498255,
            "modifiedBy": "superuser",
            "value": "flying",
            "questionId": 2
          },
          {
            "id": 4,
            "version": 1,
            "created": 1619607498255,
            "createdBy": "superuser",
            "modified": 1619607498255,
            "modifiedBy": "superuser",
            "value": "dev",
            "questionId": 12
          },
          {
            "id": 5,
            "version": 1,
            "created": 1619607498255,
            "createdBy": "superuser",
            "modified": 1619607498255,
            "modifiedBy": "superuser",
            "value": "life",
            "questionId": 14
          },
          {
            "id": 6,
            "version": 1,
            "created": 1619607498255,
            "createdBy": "superuser",
            "modified": 1619607498255,
            "modifiedBy": "superuser",
            "value": "8>",
            "questionId": 11
          },
          {
            "id": 7,
            "version": 1,
            "created": 1619607498255,
            "createdBy": "superuser",
            "modified": 1619607498255,
            "modifiedBy": "superuser",
            "value": "grandparent",
            "questionId": 9
          },
          {
            "id": 8,
            "version": 1,
            "created": 1619607498255,
            "createdBy": "superuser",
            "modified": 1619607498255,
            "modifiedBy": "superuser",
            "value": "individual",
            "questionId": 4
          },
          {
            "id": 9,
            "version": 1,
            "created": 1619607498255,
            "createdBy": "superuser",
            "modified": 1619607498255,
            "modifiedBy": "superuser",
            "value": "no",
            "questionId": 7
          },
          {
            "id": 10,
            "version": 1,
            "created": 1619607498255,
            "createdBy": "superuser",
            "modified": 1619607498255,
            "modifiedBy": "superuser",
            "value": "powerful",
            "questionId": 5
          },
          {
            "id": 11,
            "version": 1,
            "created": 1619607498255,
            "createdBy": "superuser",
            "modified": 1619607498255,
            "modifiedBy": "superuser",
            "value": "future",
            "questionId": 6
          },
          {
            "id": 12,
            "version": 1,
            "created": 1619607498255,
            "createdBy": "superuser",
            "modified": 1619607498255,
            "modifiedBy": "superuser",
            "value": "WTF",
            "questionId": 3
          },
          {
            "id": 13,
            "version": 1,
            "created": 1619607498255,
            "createdBy": "superuser",
            "modified": 1619607498255,
            "modifiedBy": "superuser",
            "value": "father",
            "questionId": 13
          },
          {
            "id": 14,
            "version": 1,
            "created": 1619607498255,
            "createdBy": "superuser",
            "modified": 1619607498255,
            "modifiedBy": "superuser",
            "value": "bitcoin",
            "questionId": 15
          },
          {
            "id": 15,
            "version": 1,
            "created": 1619607498255,
            "createdBy": "superuser",
            "modified": 1619607498255,
            "modifiedBy": "superuser",
            "value": "night-owl",
            "questionId": 1
          }
        ],
        "formId": 1
      },
      {
        "id": 2,
        "version": 1,
        "created": 1619780316370,
        "createdBy": "superuser",
        "modified": 1619780316370,
        "modifiedBy": "superuser",
        "items": [
          {
            "id": 16,
            "version": 1,
            "created": 1619780316372,
            "createdBy": "superuser",
            "modified": 1619780316372,
            "modifiedBy": "superuser",
            "value": "hr",
            "questionId": 12
          },
          {
            "id": 17,
            "version": 1,
            "created": 1619780316372,
            "createdBy": "superuser",
            "modified": 1619780316372,
            "modifiedBy": "superuser",
            "value": "teenager",
            "questionId": 9
          },
          {
            "id": 18,
            "version": 1,
            "created": 1619780316372,
            "createdBy": "superuser",
            "modified": 1619780316372,
            "modifiedBy": "superuser",
            "value": "father",
            "questionId": 13
          },
          {
            "id": 19,
            "version": 1,
            "created": 1619780316372,
            "createdBy": "superuser",
            "modified": 1619780316372,
            "modifiedBy": "superuser",
            "value": "wander",
            "questionId": 7
          },
          {
            "id": 20,
            "version": 1,
            "created": 1619780316373,
            "createdBy": "superuser",
            "modified": 1619780316373,
            "modifiedBy": "superuser",
            "value": "SOS",
            "questionId": 3
          },
          {
            "id": 21,
            "version": 1,
            "created": 1619780316373,
            "createdBy": "superuser",
            "modified": 1619780316373,
            "modifiedBy": "superuser",
            "value": "children",
            "questionId": 14
          },
          {
            "id": 22,
            "version": 1,
            "created": 1619780316373,
            "createdBy": "superuser",
            "modified": 1619780316373,
            "modifiedBy": "superuser",
            "value": "grandparent",
            "questionId": 9
          },
          {
            "id": 23,
            "version": 1,
            "created": 1619780316373,
            "createdBy": "superuser",
            "modified": 1619780316373,
            "modifiedBy": "superuser",
            "value": "Thomas Edison",
            "questionId": 10
          },
          {
            "id": 24,
            "version": 1,
            "created": 1619780316373,
            "createdBy": "superuser",
            "modified": 1619780316373,
            "modifiedBy": "superuser",
            "value": "Autonomous Vehicles",
            "questionId": 15
          },
          {
            "id": 25,
            "version": 1,
            "created": 1619780316373,
            "createdBy": "superuser",
            "modified": 1619780316373,
            "modifiedBy": "superuser",
            "value": "other",
            "questionId": 8
          },
          {
            "id": 26,
            "version": 1,
            "created": 1619780316373,
            "createdBy": "superuser",
            "modified": 1619780316373,
            "modifiedBy": "superuser",
            "value": "individual",
            "questionId": 4
          },
          {
            "id": 27,
            "version": 1,
            "created": 1619780316373,
            "createdBy": "superuser",
            "modified": 1619780316373,
            "modifiedBy": "superuser",
            "value": "8>",
            "questionId": 11
          },
          {
            "id": 28,
            "version": 1,
            "created": 1619780316373,
            "createdBy": "superuser",
            "modified": 1619780316373,
            "modifiedBy": "superuser",
            "value": "clear",
            "questionId": 5
          },
          {
            "id": 29,
            "version": 1,
            "created": 1619780316373,
            "createdBy": "superuser",
            "modified": 1619780316373,
            "modifiedBy": "superuser",
            "value": "early-bird",
            "questionId": 1
          },
          {
            "id": 30,
            "version": 1,
            "created": 1619780316373,
            "createdBy": "superuser",
            "modified": 1619780316373,
            "modifiedBy": "superuser",
            "value": "invisibility",
            "questionId": 2
          },
          {
            "id": 31,
            "version": 1,
            "created": 1619780316373,
            "createdBy": "superuser",
            "modified": 1619780316373,
            "modifiedBy": "superuser",
            "value": "present",
            "questionId": 6
          }
        ],
        "formId": 1
      },
      {
        "id": 3,
        "version": 1,
        "created": 1619607526407,
        "createdBy": "superuser",
        "modified": 1619780326407,
        "modifiedBy": "superuser",
        "items": [
          {
            "id": 32,
            "version": 1,
            "created": 1619780326408,
            "createdBy": "superuser",
            "modified": 1619780326408,
            "modifiedBy": "superuser",
            "value": "8>",
            "questionId": 11
          },
          {
            "id": 33,
            "version": 1,
            "created": 1619780326408,
            "createdBy": "superuser",
            "modified": 1619780326408,
            "modifiedBy": "superuser",
            "value": "grandparent",
            "questionId": 9
          },
          {
            "id": 34,
            "version": 1,
            "created": 1619780326409,
            "createdBy": "superuser",
            "modified": 1619780326409,
            "modifiedBy": "superuser",
            "value": "Nikola Tesla",
            "questionId": 10
          },
          {
            "id": 35,
            "version": 1,
            "created": 1619780326409,
            "createdBy": "superuser",
            "modified": 1619780326409,
            "modifiedBy": "superuser",
            "value": "future",
            "questionId": 6
          },
          {
            "id": 36,
            "version": 1,
            "created": 1619780326409,
            "createdBy": "superuser",
            "modified": 1619780326409,
            "modifiedBy": "superuser",
            "value": "father",
            "questionId": 13
          },
          {
            "id": 37,
            "version": 1,
            "created": 1619780326409,
            "createdBy": "superuser",
            "modified": 1619780326409,
            "modifiedBy": "superuser",
            "value": "individual",
            "questionId": 4
          },
          {
            "id": 38,
            "version": 1,
            "created": 1619780326409,
            "createdBy": "superuser",
            "modified": 1619780326409,
            "modifiedBy": "superuser",
            "value": "dev",
            "questionId": 12
          },
          {
            "id": 39,
            "version": 1,
            "created": 1619780326409,
            "createdBy": "superuser",
            "modified": 1619780326409,
            "modifiedBy": "superuser",
            "value": "pajamas",
            "questionId": 8
          },
          {
            "id": 40,
            "version": 1,
            "created": 1619780326409,
            "createdBy": "superuser",
            "modified": 1619780326409,
            "modifiedBy": "superuser",
            "value": "powerful",
            "questionId": 5
          },
          {
            "id": 41,
            "version": 1,
            "created": 1619780326409,
            "createdBy": "superuser",
            "modified": 1619780326409,
            "modifiedBy": "superuser",
            "value": "night-owl",
            "questionId": 1
          },
          {
            "id": 42,
            "version": 1,
            "created": 1619780326409,
            "createdBy": "superuser",
            "modified": 1619780326409,
            "modifiedBy": "superuser",
            "value": "WTF",
            "questionId": 3
          },
          {
            "id": 43,
            "version": 1,
            "created": 1619780326409,
            "createdBy": "superuser",
            "modified": 1619780326409,
            "modifiedBy": "superuser",
            "value": "no",
            "questionId": 7
          },
          {
            "id": 44,
            "version": 1,
            "created": 1619780326409,
            "createdBy": "superuser",
            "modified": 1619780326409,
            "modifiedBy": "superuser",
            "value": "life",
            "questionId": 14
          },
          {
            "id": 45,
            "version": 1,
            "created": 1619780326409,
            "createdBy": "superuser",
            "modified": 1619780326409,
            "modifiedBy": "superuser",
            "value": "bitcoin",
            "questionId": 15
          },
          {
            "id": 46,
            "version": 1,
            "created": 1619780326409,
            "createdBy": "superuser",
            "modified": 1619780326409,
            "modifiedBy": "superuser",
            "value": "flying",
            "questionId": 2
          }
        ],
        "formId": 1
      },
      {
        "id": 4,
        "version": 1,
        "created": 1619780380936,
        "createdBy": "superuser",
        "modified": 1619780380936,
        "modifiedBy": "superuser",
        "items": [
          {
            "id": 47,
            "version": 1,
            "created": 1619780380938,
            "createdBy": "superuser",
            "modified": 1619780380938,
            "modifiedBy": "superuser",
            "value": "no",
            "questionId": 7
          },
          {
            "id": 48,
            "version": 1,
            "created": 1619780380938,
            "createdBy": "superuser",
            "modified": 1619780380938,
            "modifiedBy": "superuser",
            "value": "WTF",
            "questionId": 3
          },
          {
            "id": 49,
            "version": 1,
            "created": 1619780380938,
            "createdBy": "superuser",
            "modified": 1619780380938,
            "modifiedBy": "superuser",
            "value": "8>",
            "questionId": 11
          },
          {
            "id": 50,
            "version": 1,
            "created": 1619780380938,
            "createdBy": "superuser",
            "modified": 1619780380938,
            "modifiedBy": "superuser",
            "value": "pajamas",
            "questionId": 8
          },
          {
            "id": 51,
            "version": 1,
            "created": 1619780380938,
            "createdBy": "superuser",
            "modified": 1619780380938,
            "modifiedBy": "superuser",
            "value": "Nikola Tesla",
            "questionId": 10
          },
          {
            "id": 52,
            "version": 1,
            "created": 1619780380938,
            "createdBy": "superuser",
            "modified": 1619780380938,
            "modifiedBy": "superuser",
            "value": "individual",
            "questionId": 4
          },
          {
            "id": 53,
            "version": 1,
            "created": 1619780380938,
            "createdBy": "superuser",
            "modified": 1619780380938,
            "modifiedBy": "superuser",
            "value": "grandparent",
            "questionId": 9
          },
          {
            "id": 54,
            "version": 1,
            "created": 1619780380938,
            "createdBy": "superuser",
            "modified": 1619780380938,
            "modifiedBy": "superuser",
            "value": "bitcoin",
            "questionId": 15
          },
          {
            "id": 55,
            "version": 1,
            "created": 1619780380938,
            "createdBy": "superuser",
            "modified": 1619780380938,
            "modifiedBy": "superuser",
            "value": "dev",
            "questionId": 12
          },
          {
            "id": 56,
            "version": 1,
            "created": 1619780380938,
            "createdBy": "superuser",
            "modified": 1619780380938,
            "modifiedBy": "superuser",
            "value": "night-owl",
            "questionId": 1
          },
          {
            "id": 57,
            "version": 1,
            "created": 1619780380938,
            "createdBy": "superuser",
            "modified": 1619780380938,
            "modifiedBy": "superuser",
            "value": "powerful",
            "questionId": 5
          },
          {
            "id": 58,
            "version": 1,
            "created": 1619780380938,
            "createdBy": "superuser",
            "modified": 1619780380938,
            "modifiedBy": "superuser",
            "value": "father",
            "questionId": 13
          },
          {
            "id": 59,
            "version": 1,
            "created": 1619780380938,
            "createdBy": "superuser",
            "modified": 1619780380938,
            "modifiedBy": "superuser",
            "value": "life",
            "questionId": 14
          },
          {
            "id": 60,
            "version": 1,
            "created": 1619780380938,
            "createdBy": "superuser",
            "modified": 1619780380938,
            "modifiedBy": "superuser",
            "value": "future",
            "questionId": 6
          },
          {
            "id": 61,
            "version": 1,
            "created": 1619780380938,
            "createdBy": "superuser",
            "modified": 1619780380938,
            "modifiedBy": "superuser",
            "value": "flying",
            "questionId": 2
          }
        ],
        "formId": 1
      },
      {
        "id": 5,
        "version": 1,
        "created": 1619780444446,
        "createdBy": "anonymous",
        "modified": 1619780444446,
        "modifiedBy": "anonymous",
        "items": [
          {
            "id": 62,
            "version": 1,
            "created": 1619780444447,
            "createdBy": "anonymous",
            "modified": 1619780444447,
            "modifiedBy": "anonymous",
            "value": "powerful",
            "questionId": 5
          },
          {
            "id": 63,
            "version": 1,
            "created": 1619780444447,
            "createdBy": "anonymous",
            "modified": 1619780444447,
            "modifiedBy": "anonymous",
            "value": "flying",
            "questionId": 2
          },
          {
            "id": 64,
            "version": 1,
            "created": 1619780444447,
            "createdBy": "anonymous",
            "modified": 1619780444447,
            "modifiedBy": "anonymous",
            "value": "grandparent",
            "questionId": 9
          },
          {
            "id": 65,
            "version": 1,
            "created": 1619780444447,
            "createdBy": "anonymous",
            "modified": 1619780444447,
            "modifiedBy": "anonymous",
            "value": "father",
            "questionId": 13
          },
          {
            "id": 66,
            "version": 1,
            "created": 1619780444447,
            "createdBy": "anonymous",
            "modified": 1619780444447,
            "modifiedBy": "anonymous",
            "value": "dev",
            "questionId": 12
          },
          {
            "id": 67,
            "version": 1,
            "created": 1619780444447,
            "createdBy": "anonymous",
            "modified": 1619780444447,
            "modifiedBy": "anonymous",
            "value": "life",
            "questionId": 14
          },
          {
            "id": 68,
            "version": 1,
            "created": 1619780444447,
            "createdBy": "anonymous",
            "modified": 1619780444447,
            "modifiedBy": "anonymous",
            "value": "WTF",
            "questionId": 3
          },
          {
            "id": 69,
            "version": 1,
            "created": 1619780444447,
            "createdBy": "anonymous",
            "modified": 1619780444447,
            "modifiedBy": "anonymous",
            "value": "no",
            "questionId": 7
          },
          {
            "id": 70,
            "version": 1,
            "created": 1619780444447,
            "createdBy": "anonymous",
            "modified": 1619780444447,
            "modifiedBy": "anonymous",
            "value": "night-owl",
            "questionId": 1
          },
          {
            "id": 71,
            "version": 1,
            "created": 1619780444447,
            "createdBy": "anonymous",
            "modified": 1619780444447,
            "modifiedBy": "anonymous",
            "value": "bitcoin",
            "questionId": 15
          },
          {
            "id": 72,
            "version": 1,
            "created": 1619780444447,
            "createdBy": "anonymous",
            "modified": 1619780444447,
            "modifiedBy": "anonymous",
            "value": "individual",
            "questionId": 4
          },
          {
            "id": 73,
            "version": 1,
            "created": 1619780444448,
            "createdBy": "anonymous",
            "modified": 1619780444448,
            "modifiedBy": "anonymous",
            "value": "Nikola Tesla",
            "questionId": 10
          },
          {
            "id": 74,
            "version": 1,
            "created": 1619780444448,
            "createdBy": "anonymous",
            "modified": 1619780444448,
            "modifiedBy": "anonymous",
            "value": "future",
            "questionId": 6
          },
          {
            "id": 75,
            "version": 1,
            "created": 1619780444448,
            "createdBy": "anonymous",
            "modified": 1619780444448,
            "modifiedBy": "anonymous",
            "value": "pajamas",
            "questionId": 8
          },
          {
            "id": 76,
            "version": 1,
            "created": 1619780444448,
            "createdBy": "anonymous",
            "modified": 1619780444448,
            "modifiedBy": "anonymous",
            "value": "8>",
            "questionId": 11
          }
        ],
        "formId": 1
      },
      {
        "id": 6,
        "version": 1,
        "created": 1620635015426,
        "createdBy": "anonymous",
        "modified": 1620635015426,
        "modifiedBy": "anonymous",
        "items": [
          {
            "id": 77,
            "version": 1,
            "created": 1620635015458,
            "createdBy": "anonymous",
            "modified": 1620635015458,
            "modifiedBy": "anonymous",
            "value": "8>",
            "questionId": 11
          },
          {
            "id": 78,
            "version": 1,
            "created": 1620635015471,
            "createdBy": "anonymous",
            "modified": 1620635015471,
            "modifiedBy": "anonymous",
            "value": "pajamas",
            "questionId": 8
          },
          {
            "id": 79,
            "version": 1,
            "created": 1620635015471,
            "createdBy": "anonymous",
            "modified": 1620635015471,
            "modifiedBy": "anonymous",
            "value": "flying",
            "questionId": 2
          },
          {
            "id": 80,
            "version": 1,
            "created": 1620635015471,
            "createdBy": "anonymous",
            "modified": 1620635015471,
            "modifiedBy": "anonymous",
            "value": "bitcoin",
            "questionId": 15
          },
          {
            "id": 81,
            "version": 1,
            "created": 1620635015471,
            "createdBy": "anonymous",
            "modified": 1620635015471,
            "modifiedBy": "anonymous",
            "value": "no",
            "questionId": 7
          },
          {
            "id": 82,
            "version": 1,
            "created": 1620635015471,
            "createdBy": "anonymous",
            "modified": 1620635015471,
            "modifiedBy": "anonymous",
            "value": "life",
            "questionId": 14
          },
          {
            "id": 83,
            "version": 1,
            "created": 1620635015471,
            "createdBy": "anonymous",
            "modified": 1620635015471,
            "modifiedBy": "anonymous",
            "value": "powerful",
            "questionId": 5
          },
          {
            "id": 84,
            "version": 1,
            "created": 1620635015471,
            "createdBy": "anonymous",
            "modified": 1620635015471,
            "modifiedBy": "anonymous",
            "value": "dev",
            "questionId": 12
          },
          {
            "id": 85,
            "version": 1,
            "created": 1620635015471,
            "createdBy": "anonymous",
            "modified": 1620635015471,
            "modifiedBy": "anonymous",
            "value": "Nikola Tesla",
            "questionId": 10
          },
          {
            "id": 86,
            "version": 1,
            "created": 1620635015471,
            "createdBy": "anonymous",
            "modified": 1620635015471,
            "modifiedBy": "anonymous",
            "value": "grandparent",
            "questionId": 9
          },
          {
            "id": 87,
            "version": 1,
            "created": 1620635015471,
            "createdBy": "anonymous",
            "modified": 1620635015471,
            "modifiedBy": "anonymous",
            "value": "father",
            "questionId": 13
          },
          {
            "id": 88,
            "version": 1,
            "created": 1620635015471,
            "createdBy": "anonymous",
            "modified": 1620635015471,
            "modifiedBy": "anonymous",
            "value": "WTF",
            "questionId": 3
          },
          {
            "id": 89,
            "version": 1,
            "created": 1620635015471,
            "createdBy": "anonymous",
            "modified": 1620635015471,
            "modifiedBy": "anonymous",
            "value": "individual",
            "questionId": 4
          },
          {
            "id": 90,
            "version": 1,
            "created": 1620635015471,
            "createdBy": "anonymous",
            "modified": 1620635015471,
            "modifiedBy": "anonymous",
            "value": "future",
            "questionId": 6
          },
          {
            "id": 91,
            "version": 1,
            "created": 1620635015471,
            "createdBy": "anonymous",
            "modified": 1620635015471,
            "modifiedBy": "anonymous",
            "value": "night-owl",
            "questionId": 1
          }
        ],
        "formId": 1
      }
    ]
    ```
</details>
<br/><br/><br/>
