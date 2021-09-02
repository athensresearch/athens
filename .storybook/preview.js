import React from 'react';
import { themes } from '@storybook/theming';

import { GlobalStyles } from '../src/js/style/style';
import { badges, Storybook } from '../src/js/storybook';

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
      appContentBg: '#fff',
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
    console.log(context);
    return (
      <>
        <GlobalStyles />
        <Storybook.App
          id="app"
          className={[
            context.globals.theme === 'light' ? 'is-theme-light' : 'is-theme-dark',
            context.globals.hostType === 'electron' ? 'is-electron' : 'is-browser',
            context.viewMode === 'docs' ? 'is-storybook-docs' : 'is-storybook-canvas'
          ].join(' ')}>
          <Story />
        </Storybook.App>
      </>
    )
  },
];