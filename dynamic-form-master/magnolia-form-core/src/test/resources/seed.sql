--
-- Dumping data for table `forms`
--
INSERT INTO `forms`
VALUES
    (
        '2ccd98a1-9081-4beb-a954-c2aa2b3b4248',
        'Example form', 'Poll with different types of questions.\nPlease answer the questions bellow!',
        0, 0, 1, 1, NULL, '[]', '{}', 1, '2022-10-07 10:02:47.045000',
        'superuser', '2022-10-07 10:02:47.045000',
        'superuser'
    );

--
-- Dumping data for table `forms_localized`
--

--
-- Dumping data for table `sections`
--
INSERT INTO `sections`
VALUES
    (
        '3fc5f7ea-4991-4731-8cc3-2e359aac25de',
        'Word cloud', 'Section of free text questions.',
        '2ccd98a1-9081-4beb-a954-c2aa2b3b4248',
        1, '{}', 1, '2022-10-07 10:03:04.542000',
        'superuser', '2022-10-07 10:03:52.878000',
        'superuser'
    ),
    (
        '462e65cd-336f-42c4-8e51-d26dc7097089',
        'Multi option', 'Section of multi choice questions.',
        '2ccd98a1-9081-4beb-a954-c2aa2b3b4248',
        3, '{}', 1, '2022-10-07 10:03:46.705000',
        'superuser', '2022-10-07 10:03:46.705000',
        'superuser'
    ),
    (
        '873aff85-1b08-463f-941b-a960bb37eb05',
        'Single option', 'Section of single choice questions.',
        '2ccd98a1-9081-4beb-a954-c2aa2b3b4248',
        2, '{}', 1, '2022-10-07 10:03:26.567000',
        'superuser', '2022-10-07 10:03:26.567000',
        'superuser'
    );

--
-- Dumping data for table `sections_localized`
--

--
-- Dumping data for table `questions`
--
INSERT INTO `questions`
VALUES
    (
        '00687337-f69a-4b0b-a2b4-60b69eae01cf',
        'Project priority', 'Which projects should we prioritize this quarter?',
        'multi', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
        '462e65cd-336f-42c4-8e51-d26dc7097089',
        NULL, 1, '{}', 1, '2022-10-07 10:10:23.963000',
        'superuser', '2022-10-07 10:10:23.963000',
        'superuser'
    ),
    (
        '04fd750f-b958-4f08-bf6b-622b6c87ef52',
        'Improving areas', 'Which of these areas should we focus on improving?',
        'multi', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
        '462e65cd-336f-42c4-8e51-d26dc7097089',
        NULL, 5, '{}', 1, '2022-10-07 10:12:06.389000',
        'superuser', '2022-10-07 10:12:06.389000',
        'superuser'
    ),
    (
        '380e4c1c-6858-4842-b76e-8d7279b6f318',
        'Time-travel', 'If you could time-travel, which period would you go to?',
        'single', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
        '873aff85-1b08-463f-941b-a960bb37eb05',
        NULL, 3, '{}', 1, '2022-10-07 10:09:07.558000',
        'superuser', '2022-10-07 10:09:07.558000',
        'superuser'
    ),
    (
        '502a3270-3e81-45a2-a4d4-b81571c20150',
        'State of mind', 'If age is only a state of mind, which category best describes your state of mind right now?',
        'multi', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
        '462e65cd-336f-42c4-8e51-d26dc7097089',
        NULL, 4, '{}', 1, '2022-10-07 10:11:41.525000',
        'superuser', '2022-10-07 10:11:41.525000',
        'superuser'
    ),
    (
        '837025e9-4002-4943-9d0c-e35c62f92942',
        'Grateful for', 'What are you most grateful for?',
        'text', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
        '3fc5f7ea-4991-4731-8cc3-2e359aac25de',
        NULL, 4, '{}', 1, '2022-10-07 10:07:21.265000',
        'superuser', '2022-10-07 10:07:21.265000',
        'superuser'
    ),
    (
        'ae27d6ef-d0c9-48b8-bc4f-a621930f36b5',
        'Inspires you', 'Which industry figure inspires you?',
        'text', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
        '3fc5f7ea-4991-4731-8cc3-2e359aac25de',
        NULL, 2, '{}', 1, '2022-10-07 10:06:40.349000',
        'superuser', '2022-10-07 10:06:40.349000',
        'superuser'
    ),
    (
        'b5ad27d4-9570-4226-aa4f-f7976ed27f3b',
        'Superpower', 'Which superpower would you like to have?',
        'single', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
        '873aff85-1b08-463f-941b-a960bb37eb05',
        NULL, 2, '{}', 1, '2022-10-07 10:08:48.741000',
        'superuser', '2022-10-07 10:08:48.741000',
        'superuser'
    ),
    (
        'c23458d4-80bb-4e74-bb05-4dc1c2ccd105',
        'Multitask', 'Do you multitask when attending a meeting online?',
        'single', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
        '873aff85-1b08-463f-941b-a960bb37eb05',
        NULL, 4, '{}', 1, '2022-10-07 10:09:27.073000',
        'superuser', '2022-10-07 10:09:27.073000',
        'superuser'
    ),
    (
        'c75790a6-6f74-4a1c-8508-5f0bdca4c1dc',
        'State of mind', 'Using one word, what’s your state of mind right now?',
        'text', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
        '3fc5f7ea-4991-4731-8cc3-2e359aac25de',
        NULL, 1, '{}', 1, '2022-10-07 10:05:01.926000',
        'superuser', '2022-10-07 10:05:01.926000',
        'superuser'
    ),
    (
        'd712ca28-b50a-4302-b692-483e67617365',
        'Tech invention', 'What’s the best tech invention of the 21st Century?',
        'text', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
        '3fc5f7ea-4991-4731-8cc3-2e359aac25de',
        NULL, 5, '{}', 1, '2022-10-07 10:07:43.992000',
        'superuser', '2022-10-07 10:07:43.992000',
        'superuser'
    ),
    (
        'd8b56533-2391-423a-950a-a19b40a8b1bc',
        'Role model', 'Who is your role model?',
        'text', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
        '3fc5f7ea-4991-4731-8cc3-2e359aac25de',
        NULL, 3, '{}', 1, '2022-10-07 10:07:03.942000',
        'superuser', '2022-10-07 10:07:03.942000',
        'superuser'
    ),
    (
        'dfc8a09b-1112-4f4f-a618-2a9255496d31',
        'Early bird', 'Are you an early bird or a night owl?',
        'single', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
        '873aff85-1b08-463f-941b-a960bb37eb05',
        NULL, 1, '{}', 1, '2022-10-07 10:08:25.462000',
        'superuser', '2022-10-07 10:08:25.462000',
        'superuser'
    ),
    (
        'e2440662-89d8-4eaa-adcd-3d65df31b757',
        'Strangest thing', 'What’s the strangest thing you did while attending a meeting online?',
        'multi', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
        '462e65cd-336f-42c4-8e51-d26dc7097089',
        NULL, 3, '{}', 1, '2022-10-07 10:11:13.614000',
        'superuser', '2022-10-07 10:11:13.614000',
        'superuser'
    ),
    (
        'e77aed2e-494c-4e6f-8bd6-9a647bbf42e1',
        'Responsible for L&D', 'Who should be responsible for L&D?',
        'multi', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
        '462e65cd-336f-42c4-8e51-d26dc7097089',
        NULL, 2, '{}', 1, '2022-10-07 10:10:40.755000',
        'superuser', '2022-10-07 10:10:40.755000',
        'superuser'
    ),
    (
        'ea13f315-3988-4511-981f-981aedb0229e',
        'Hours online', 'How many hours a day do you spend online?',
        'single', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
        '873aff85-1b08-463f-941b-a960bb37eb05',
        NULL, 5, '{}', 1, '2022-10-07 10:09:51.350000',
        'superuser', '2022-10-07 10:09:51.350000',
        'superuser'
    );

--
-- Dumping data for table `questions_localized`
--

--
-- Dumping data for table `answer_options`
--
INSERT INTO `answer_options`
VALUES
    (
        '0160fa8d-fd7d-4010-adc8-f38a4e5397d1',
        'Groovy grandparent', 'Groovy grandparent',
        'grandparent', NULL, 'dontShowFreeText',
        NULL, '502a3270-3e81-45a2-a4d4-b81571c20150',
        4, '{}', 1, '2022-10-07 10:42:43.449000',
        'superuser', '2022-10-07 10:42:43.449000',
        'superuser'
    ),
    (
        '0930595f-63dc-43cd-bb79-eec13c30d37a',
        'Team lead', 'Team lead', 'team-lead',
        NULL, 'dontShowFreeText', NULL, 'e77aed2e-494c-4e6f-8bd6-9a647bbf42e1',
        2, '{}', 1, '2022-10-07 10:37:13.713000',
        'superuser', '2022-10-07 10:37:13.713000',
        'superuser'
    ),
    (
        '0b96c0d5-4864-42d6-a442-88aa331afc53',
        'Wore pajamas', 'Wore pajamas',
        'pajamas', NULL, 'dontShowFreeText', NULL,
        'e2440662-89d8-4eaa-adcd-3d65df31b757',
        2, '{}', 1, '2022-10-07 10:40:11.202000',
        'superuser', '2022-10-07 10:40:11.202000',
        'superuser'
    ),
    (
        '0df9c286-713f-4844-b22e-17d8d516d6a9',
        'Cheeky child', 'Cheeky child',
        'child', NULL, 'dontShowFreeText', NULL,
        '502a3270-3e81-45a2-a4d4-b81571c20150',
        1, '{}', 1, '2022-10-07 10:41:54.114000',
        'superuser', '2022-10-07 10:41:54.114000',
        'superuser'
    ),
    (
        '14c67460-c848-4524-8c05-f07949c536ad',
        'Technical development', 'Technical development',
        'dev', NULL, 'dontShowFreeText', NULL,
        '04fd750f-b958-4f08-bf6b-622b6c87ef52',
        2, '{}', 1, '2022-10-07 10:38:58.397000',
        'superuser', '2022-10-07 10:38:58.397000',
        'superuser'
    ),
    (
        '213d5934-eea1-45d8-bd94-b64027a4e7be',
        '1-2 hours', '1-2 hours', '1-2',
        NULL, 'dontShowFreeText', NULL, 'ea13f315-3988-4511-981f-981aedb0229e',
        1, '{}', 1, '2022-10-07 10:18:48.687000',
        'superuser', '2022-10-07 10:18:48.687000',
        'superuser'
    ),
    (
        '22cef728-0b30-4c04-ae6d-d0847173366c',
        'HR', 'HR', 'hr', NULL, 'dontShowFreeText',
        NULL, 'e77aed2e-494c-4e6f-8bd6-9a647bbf42e1',
        3, '{}', 1, '2022-10-07 10:37:28.835000',
        'superuser', '2022-10-07 10:37:28.835000',
        'superuser'
    ),
    (
        '28fddbbc-c13c-4326-875b-8a20c986ac8e',
        'I’m good where I am', 'I’m good where I am',
        'present', NULL, 'dontShowFreeText', NULL,
        '380e4c1c-6858-4842-b76e-8d7279b6f318',
        3, '{}', 1, '2022-10-07 10:16:39.810000',
        'superuser', '2022-10-07 10:16:39.810000',
        'superuser'
    ),
    (
        '38b48c42-73cb-4216-8191-510d52e7e03d',
        'My mind tends to wander', 'My mind tends to wander',
        'wander', NULL, 'dontShowFreeText', NULL,
        'c23458d4-80bb-4e74-bb05-4dc1c2ccd105',
        2, '{}', 1, '2022-10-07 10:17:43.532000',
        'superuser', '2022-10-07 10:17:43.532000',
        'superuser'
    ),
    (
        '3b7fd10a-cfb6-4668-8261-d15c4b2c8197',
        'I lost count', 'I lost count',
        '8>', NULL, 'dontShowFreeText', NULL, 'ea13f315-3988-4511-981f-981aedb0229e',
        4, '{}', 1, '2022-10-07 10:19:35.553000',
        'superuser', '2022-10-07 10:19:35.553000',
        'superuser'
    ),
    (
        '42a63f4d-abe7-400f-84d1-2fd6d161ddd8',
        'Mind reading', 'Mind reading',
        'mind-reading', NULL, 'dontShowFreeText',
        NULL, 'b5ad27d4-9570-4226-aa4f-f7976ed27f3b',
        1, '{}', 1, '2022-10-07 10:14:47.265000',
        'superuser', '2022-10-07 10:14:47.265000',
        'superuser'
    ),
    (
        '48c8b848-c6b4-4e35-98ab-e90012f06269',
        'Tormented teenager', 'Tormented teenager',
        'teenager', NULL, 'dontShowFreeText',
        NULL, '502a3270-3e81-45a2-a4d4-b81571c20150',
        2, '{}', 1, '2022-10-07 10:42:08.613000',
        'superuser', '2022-10-07 10:42:08.613000',
        'superuser'
    ),
    (
        '5511ff2f-2059-4063-b5b9-36d4dfdd18a0',
        'Sometimes', 'Sometimes', 'sometimes',
        NULL, 'dontShowFreeText', NULL, 'c23458d4-80bb-4e74-bb05-4dc1c2ccd105',
        4, '{}', 1, '2022-10-07 10:18:13.982000',
        'superuser', '2022-10-07 10:18:13.982000',
        'superuser'
    ),
    (
        '5e5716e4-7221-4c16-8edd-997a9a3d452c',
        'The future', 'The future', 'future',
        NULL, 'dontShowFreeText', NULL, '380e4c1c-6858-4842-b76e-8d7279b6f318',
        2, '{}', 1, '2022-10-07 10:16:22.395000',
        'superuser', '2022-10-07 10:16:22.395000',
        'superuser'
    ),
    (
        '5e74e34f-36ec-4518-91df-0344dbd098b5',
        'Teleportation', 'Teleportation',
        'teleportation', NULL, 'dontShowFreeText',
        NULL, 'b5ad27d4-9570-4226-aa4f-f7976ed27f3b',
        3, '{}', 1, '2022-10-07 10:15:16.988000',
        'superuser', '2022-10-07 10:15:16.988000',
        'superuser'
    ),
    (
        '5f7f57ce-e394-4a51-82cd-85eb88e8e132',
        'Individual', 'Individual', 'individual',
        NULL, 'dontShowFreeText', NULL, 'e77aed2e-494c-4e6f-8bd6-9a647bbf42e1',
        1, '{}', 1, '2022-10-07 10:36:54.973000',
        'superuser', '2022-10-07 10:36:54.973000',
        'superuser'
    ),
    (
        '60836904-168d-43e7-8831-8e4a00114176',
        'Mad mid-lifer', 'Mad mid-lifer',
        'mid-lifer', NULL, 'dontShowFreeText',
        NULL, '502a3270-3e81-45a2-a4d4-b81571c20150',
        3, '{}', 1, '2022-10-07 10:42:24.060000',
        'superuser', '2022-10-07 10:42:24.060000',
        'superuser'
    ),
    (
        '66c2831f-3282-4fb0-b00c-8257d4a9e893',
        'Ate breakfast', 'Ate breakfast',
        'ate', NULL, 'dontShowFreeText', NULL,
        'e2440662-89d8-4eaa-adcd-3d65df31b757',
        1, '{}', 1, '2022-10-07 10:39:56.861000',
        'superuser', '2022-10-07 10:39:56.861000',
        'superuser'
    ),
    (
        '6c95d101-be7f-439e-bf24-d92087a90a3b',
        'Early bird', 'Early bird', 'early-bird',
        NULL, 'dontShowFreeText', NULL, 'dfc8a09b-1112-4f4f-a618-2a9255496d31',
        1, '{}', 1, '2022-10-07 10:13:47.711000',
        'superuser', '2022-10-07 10:13:47.711000',
        'superuser'
    ),
    (
        '8420e8f0-3ea6-4e53-a30f-2ae40e280733',
        'Project management', 'Project management',
        'pm', NULL, 'dontShowFreeText', NULL, '04fd750f-b958-4f08-bf6b-622b6c87ef52',
        1, '{}', 2, '2022-10-07 10:37:43.482000',
        'superuser', '2022-10-07 10:38:08.253000',
        'superuser'
    ),
    (
        '8d4c7ea6-8a27-4ced-9db7-95c753473f8d',
        'Cooked lunch/dinner', 'Cooked lunch/dinner',
        'cooked', NULL, 'dontShowFreeText', NULL,
        'e2440662-89d8-4eaa-adcd-3d65df31b757',
        3, '{}', 1, '2022-10-07 10:40:30.536000',
        'superuser', '2022-10-07 10:40:30.536000',
        'superuser'
    ),
    (
        '93cfd18f-86d0-485e-bccc-48e68f090bb3',
        'WTF', 'WTF', 'WTF', NULL, 'dontShowFreeText',
        NULL, '00687337-f69a-4b0b-a2b4-60b69eae01cf',
        4, '{}', 1, '2022-10-07 10:36:32.359000',
        'superuser', '2022-10-07 10:36:32.359000',
        'superuser'
    ),
    (
        '948d5889-3459-41b0-964f-17d0fa47f80b',
        'Watched Netflix', 'Watched Netflix',
        'netflix', NULL, 'dontShowFreeText', NULL,
        'e2440662-89d8-4eaa-adcd-3d65df31b757',
        4, '{}', 1, '2022-10-07 10:40:45.712000',
        'superuser', '2022-10-07 10:40:45.712000',
        'superuser'
    ),
    (
        '993ee94a-1224-4fa4-ad82-7fd7a05997fe',
        'ASM', 'ASM', 'ASM', NULL, 'dontShowFreeText',
        NULL, '00687337-f69a-4b0b-a2b4-60b69eae01cf',
        1, '{}', 1, '2022-10-07 10:34:30.776000',
        'superuser', '2022-10-07 10:34:30.776000',
        'superuser'
    ),
    (
        '99b0c7c1-47f5-4658-8aa4-810176556dca',
        '2-5 hours', '2-5 hours', '2-5',
        NULL, 'dontShowFreeText', NULL, 'ea13f315-3988-4511-981f-981aedb0229e',
        2, '{}', 1, '2022-10-07 10:19:02.051000',
        'superuser', '2022-10-07 10:19:02.051000',
        'superuser'
    ),
    (
        'a20b4acb-d2ef-4f28-ae22-ec1e09b9809a',
        'No, I’m 100% focused', 'No, I’m 100% focused',
        'no', NULL, 'dontShowFreeText', NULL, 'c23458d4-80bb-4e74-bb05-4dc1c2ccd105',
        3, '{}', 1, '2022-10-07 10:17:59.474000',
        'superuser', '2022-10-07 10:17:59.474000',
        'superuser'
    ),
    (
        'a2218f48-e9ad-44f6-a195-89292564d7be',
        'Strategic planning', 'Strategic planning',
        'plan', NULL, 'dontShowFreeText', NULL,
        '04fd750f-b958-4f08-bf6b-622b6c87ef52',
        4, '{}', 1, '2022-10-07 10:39:29.028000',
        'superuser', '2022-10-07 10:39:29.028000',
        'superuser'
    ),
    (
        'c39ff731-dc70-4b29-a3ea-0222eecc3cd4',
        'Yes, I’m guilty', 'Yes, I’m guilty',
        'yes', NULL, 'dontShowFreeText', NULL,
        'c23458d4-80bb-4e74-bb05-4dc1c2ccd105',
        1, '{}', 1, '2022-10-07 10:17:25.057000',
        'superuser', '2022-10-07 10:17:25.057000',
        'superuser'
    ),
    (
        'c4057cc5-2121-41de-a2c6-33c376831708',
        '5-8 hours', '5-8 hours', '5-8',
        NULL, 'dontShowFreeText', NULL, 'ea13f315-3988-4511-981f-981aedb0229e',
        3, '{}', 1, '2022-10-07 10:19:19.686000',
        'superuser', '2022-10-07 10:19:19.686000',
        'superuser'
    ),
    (
        'ce1f934f-f70f-49e9-9504-a25294bb99e0',
        'Other, but my lips are sealed',
        'Other, but my lips are sealed',
        'other', NULL, 'showFreeText', 'We would not tell anyone ...',
        'e2440662-89d8-4eaa-adcd-3d65df31b757',
        5, '{}', 1, '2022-10-07 10:41:34.201000',
        'superuser', '2022-10-07 10:41:34.201000',
        'superuser'
    ),
    (
        'cfdc681c-d6c8-48a2-af5d-4e5d6eb26fe1',
        'Night owl', 'Night owl', 'night-owl',
        NULL, 'dontShowFreeText', NULL, 'dfc8a09b-1112-4f4f-a618-2a9255496d31',
        2, '{}', 1, '2022-10-07 10:14:09.863000',
        'superuser', '2022-10-07 10:14:09.863000',
        'superuser'
    ),
    (
        'd8d65309-d64d-4d72-982d-06b8779df7f9',
        'Human resources', 'Human resources',
        'hr', NULL, 'dontShowFreeText', NULL, '04fd750f-b958-4f08-bf6b-622b6c87ef52',
        3, '{}', 1, '2022-10-07 10:39:12.210000',
        'superuser', '2022-10-07 10:39:12.210000',
        'superuser'
    ),
    (
        'db3749e3-0a46-448d-bcc8-0d4fe8b5ee3c',
        'Flying', 'Flying', 'flying', NULL, 'dontShowFreeText',
        NULL, 'b5ad27d4-9570-4226-aa4f-f7976ed27f3b',
        4, '{}', 1, '2022-10-07 10:15:32.309000',
        'superuser', '2022-10-07 10:15:32.309000',
        'superuser'
    ),
    (
        'e5af63dd-54d5-4705-9ce4-2f36d22963b6',
        'SOS', 'SOS', 'SOS', NULL, 'dontShowFreeText',
        NULL, '00687337-f69a-4b0b-a2b4-60b69eae01cf',
        3, '{}', 1, '2022-10-07 10:36:21.848000',
        'superuser', '2022-10-07 10:36:21.848000',
        'superuser'
    ),
    (
        'eb2ee5c7-0880-4d48-a8f2-a8809d38df80',
        'The past', 'The past', 'past', NULL, 'dontShowFreeText',
        NULL, '380e4c1c-6858-4842-b76e-8d7279b6f318',
        1, '{}', 1, '2022-10-07 10:16:05.505000',
        'superuser', '2022-10-07 10:16:05.505000',
        'superuser'
    ),
    (
        'f68210aa-cd69-4b4a-83fb-4232e16aae5d',
        'Invisibility', 'Invisibility',
        'invisibility', NULL, 'dontShowFreeText',
        NULL, 'b5ad27d4-9570-4226-aa4f-f7976ed27f3b',
        2, '{}', 1, '2022-10-07 10:15:02.708000',
        'superuser', '2022-10-07 10:15:02.708000',
        'superuser'
    ),
    (
        'f705a819-1a1c-435f-abc9-21966ed918bc',
        'MMF', 'MMF', 'MMF', NULL, 'dontShowFreeText',
        NULL, '00687337-f69a-4b0b-a2b4-60b69eae01cf',
        2, '{}', 1, '2022-10-07 10:36:11.784000',
        'superuser', '2022-10-07 10:36:11.784000',
        'superuser'
    );

--
-- Dumping data for table `answer_options_localized`
--

--
-- Dumping data for table `response_items`
--

--
-- Dumping data for table `responses`
--

