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
 z-index: 0;

 .docs-story & {
    margin: -30px -20px;
  }
`;

const Wrapper = styled.div`
  padding: 2rem;
`;

const Desktop = styled(Wrapper)`
  padding: 2rem;
  background: rebeccapurple;

  .is-storybook-canvas & {
    min-height: 100vh;
    display: flex;

    #app-layout {
      height: 100%;
    }
  }
`;

Storybook.App = App;
Storybook.Desktop = Desktop;
Storybook.Wrapper = Wrapper;
