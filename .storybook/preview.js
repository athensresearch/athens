import React from 'react';

import { themes } from '@storybook/theming';
import { ChakraProvider } from '@chakra-ui/react';


import { theme } from '../src/js/theme/theme';
import { badges, Storybook } from '../src/js/components/utils/storybook';

export const parameters = {
  actions: { argTypesRegex: "^on[A-Z].*" },
  layout: 'fullscreen',
  controls: {
    matchers: {
      color: /(background|color)$/i,
      date: /Date$/,
    },
  },
  badgesConfig: {
    ...badges
  },
  darkMode: {
    // Override the default dark theme
    dark: {
      ...themes.dark,
      appBg: '#151515',
      appContentBg: '#1A1A1A',
    },
    // Override the default light theme
    light: {
      ...themes.normal,
      appBg: '#EFEDEB',
      appContentBg: "#F6F6F6"
    }
  }
}

export const globalTypes = {
  theme: {
    name: 'Story Theme',
    description: 'Global theme for components',
    defaultValue: 'light',
    toolbar: {
      // Array of plain string values or MenuItem shape (see below)
      items: ['light', 'dark'],
      // Property that specifies if the name of the item will be displayed
      showName: true,
    },
  },
};


export const decorators = [
  (Story, context) => {

    React.useEffect(() => {
      const theme = context.globals.theme === 'dark' ? 'is-theme-dark' : 'is-theme-light';
      document.body.classList.add(theme);
      return () => document.body.classList.remove(theme);
    }, [context])

    console.log(context);
    return (
      <>
        <ChakraProvider>
          <Storybook.App
            id="app" >
            <Story />
          </Storybook.App>
        </ChakraProvider>
      </>
    )
  },
];
