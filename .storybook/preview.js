import React from 'react';
import styled from 'styled-components';

import { GlobalStyles } from '../src/js/style/style';

export const parameters = {
  actions: { argTypesRegex: "^on[A-Z].*" },
  layout: 'fullscreen',
  controls: {
    matchers: {
      color: /(background|color)$/i,
      date: /Date$/,
    },
  },
}

export const globalTypes = {
  theme: {
    name: 'Theme',
    description: 'Global theme for components',
    defaultValue: 'light',
    toolbar: {
      // icon: 'circlehollow',
      // Array of plain string values or MenuItem shape (see below)
      items: ['light', 'dark'],
      // Property that specifies if the name of the item will be displayed
      showName: true,
    },
  },
};

/**
 * Provides contextual classnames, colors, and typographic
 * defaults which all components expect.
 * 
 * Not used in the application, but useful for testing.
 */
const App = styled.div`
  background-color: var(--background-color);
  color: var(--body-text-color);
  position: relative;
  overflow: hidden;
`;


export const decorators = [
  (Story, context) => {
    console.log(context);
    return (
      <>
        <GlobalStyles />
        <App
          id="app"
          className={[
            context.globals.theme === 'light' ? 'is-theme-light' : 'is-theme-dark',
            context.globals.hostType === 'electron' ? 'is-electron' : 'is-browser',
            context.viewMode === 'docs' ? 'is-storybook-docs' : 'is-storybook-canvas'
          ].join(' ')}>
          <Story />
        </App>
      </>
    )
  },
];