# Mind Melter

**Version**: 2.0

## Overview

Welcome to MindMelter 2.0! Building upon the foundations of our successful first version, this advanced learning app transforms your quest for knowledge into an interactive journey. Harnessing the power of OpenAI's ChatGPT, MindMelter 2.0 offers a rich array of topics including entertainment, arts and literature, science and nature, sports and leisure, history, and geography.

Immerse yourself in a world where learning is engaging, enriching, and effortlessly fun! With MindMelter 2.0, dive deeper into any subject matter, discover new trivia, and expand your horizons, one day at a time.

## Getting Started

MindMelter 2.0 continues to be an APK file, compatible with Android devices. To install and use the app, ensure your device runs on Android version 5.0 (Lollipop) or above, which can be checked in the 'About Phone' section of your device's settings.

Installation steps are as follows:

1. Download the MindMelter APK file from the Github repository to your local machine.
2. Transfer the APK file to your Android device. You can do this using Email, Google Drive, or directly via a USB cable.
3. To install, you may need to enable installations from Unknown Sources. This can be done by navigating to your device's Security settings and checking the 'Unknown Sources' option under the 'Device Administration' tab.
4. Navigate to the location on your device where you transferred the APK file.
5. Tap on the APK file to initiate the installation process, and follow the on-screen instructions.
6. Once installed, you can launch the app and start your journey with MindMelter 2.0.

Note: To maintain the security of your device, remember to disable the 'Unknown Sources' option in your security settings after the installation.

## Screenshots

Below are some visual previews of our app's main features and interfaces:

1. ![Login](login.png)
   *Log in screen where users sign in with Amazon Cognito*

2. ![Signup](signup.png)
   *Sign up screen where new users create their accounts with Amazon Cognito*

3. ![Trivia Homepage](trivia_home.png)
   *Trivia Homepage, the landing page after logging in, where users can select their desired topic*

4. ![Trivia Chat](trivia_chat.png)
   *Trivia Chat page, where users can interact with the AI chatbot for more information on the chosen topic*

5. ![User Profile](user_profile.png)
   *User Profile page, where users can review past chat sessions and continue interacting with the app based on these conversations*

6. ![Settings](settings.png)
   *Settings page, where users can customize features such as Text To Speech and switch between Light/Dark modes*

(Note: Replace `login.png`, `signup.png`, `trivia_home.png`, `trivia_chat.png`, `user_profile.png`, `settings.png` with actual URLs of your images)

## Features

- **Login/Signup**: Our app uses Amazon Cognito for user authentication. This ensures that your data is secure while providing a smooth log in/sign up experience.
- **Trivia Homepage**: This is the main hub of the app. After logging in, users can select their desired trivia topic from the list.
- **Trivia Chat**: Users can engage in interactive dialogues with our AI chatbot to learn more about the selected topic.
- **User Profile**: Here, users can view and interact with past chat sessions, giving them the chance to revisit topics and continue learning.
- **Settings**: This page allows users to personalize the app to their preferences. This includes toggling between Text To Speech feature and switching between Light/Dark mode.

## Prerequisites

To install and use the MindMelter app, please ensure that you meet the following prerequisites:

1. An Android smartphone with version 5.0 (Lollipop) or above. You can check your Android version in your phone's settings in the 'About Phone' section.
2. Sufficient storage space on your device to install and run the app smoothly.
3. Active internet connection for real-time interaction with the AI chatbot and accessing new trivia topics.
4. Permission to install apps from Unknown Sources. This can be enabled in your phone's security settings.

Remember to check and verify these prerequisites before installing and using MindMelter. If you encounter any issues or need help, don't hesitate to reach out to us via Github issues.

## Architecture

MindMelter 2.0 continues as an Android application using AWS Amplify as its backend-as-a-service. This technology allows for user authentication, serverless functions, and easy database access. The application interacts with the OpenAI API to generate daily trivia and foster engaging chat interactions centered around the trivia across the multiple subjects it covers.

### Technologies

- Java
- Android SDK
- AWS Amplify
- DynamoDB
- Amazon Cognito
- [OpenAI API](https://platform.openai.com/)

## Change Log

2023-06-23 - v2.0

2023-06-20 - v1.0

## Credit and Collaborations

- Alex Carr
  - Created the authentication process
- Cameron Griffin
  - Wired in the TTS through a couple versions
- Matt Austin
  - Has his hands in everything but primarily the trivia and chat page
- Stephen Levesque
  - Wired up the profile page and generated the chat view window for past chats.

### Team Members

- Alex Carr
- Cameron Griffin
- Matt Austin
- Stephen Levesque