import { Box } from '@chakra-ui/react'

// .storybook/constants.js
export enum BADGE {
  BETA = 'beta',
  STABLE = 'stable',
  CONCEPT = 'concept',
  DEV = 'dev',
  IN_USE = 'in use',
}

export const badges = {
  [ BADGE.IN_USE ]: {
    color: '#fff',
    contrast: '#E1230C',
    title: 'In Use'
  },
  [ BADGE.DEV ]: {
    color: '#fff',
    contrast: '#E1230C',
    title: 'Dev'
  },
  [ BADGE.BETA ]: {
    color: '#fff',
    contrast: '#0084FF',
    title: 'Beta'
  },
  [ BADGE.STABLE ]: {
    color: '#fff',
    contrast: '#00C820',
    title: 'Stable'
  },
  [ BADGE.CONCEPT ]: {
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
const App = ({ children }) => <Box
  backgroundColor="var(--background-color)"
  color="var(--body-text-color)"
  position="relative"
  zIndex={0}
  sx={{
    ".docs-story &": {
      margin: "-30px -20px"
    }
  }}
>
  {children}
</Box>;

const Wrapper = ({ children }) => <Box
  padding="2rem"
>{children}</Box>

const Desktop = ({ children }) => <Box
  padding="2rem"
  background="rebeccapurple"
  sx={{
    ".is-storybook-canvas &": {
      minHieght: "100vh",
      display: "flex",
      "#app-layout": {
        height: "100%"
      }
    }
  }}
>{children}</Box>

Storybook.App = App;
Storybook.Desktop = Desktop;
Storybook.Wrapper = Wrapper;