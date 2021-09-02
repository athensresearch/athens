import styled from 'styled-components';

// .storybook/constants.js
export enum BADGE {
  BETA = 'beta',
  STABLE = 'stable',
  CONCEPT = 'concept',
  DEV = 'dev',
}

export const badges = {
  [BADGE.DEV]: {
    color: '#fff',
    contrast: '#E1230C',
    title: 'Dev'
  },
  [BADGE.BETA]: {
    color: '#fff',
    contrast: '#0084FF',
    title: 'Beta'
  },
  [BADGE.STABLE]: {
    color: '#fff',
    contrast: '#00C820',
    title: 'Stable'
  },
  [BADGE.CONCEPT]: {
    color: '#fff',
    contrast: '#FF6B00',
    title: 'Concept'
  },
};

export const Storybook = () => null;

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

 .docs-story & {
    margin: -30px -20px;
  }
`;

const Wrapper = styled.div`
  padding: 2rem;
`;

Storybook.App = App;
Storybook.Wrapper = Wrapper;
