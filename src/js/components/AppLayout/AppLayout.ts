import styled from 'styled-components';

/**
 * Provides grid for high-level app layout
 */
export const AppLayout = styled.div.attrs({ id: 'app-layout' })`
  --app-upper-spacing: 2.5rem;
  display: grid;
  grid-template-areas: 'app-header app-header app-header'
  'left-sidebar main-content secondary-content'
  'devtool devtool devtool';
  grid-template-columns: auto 1fr auto;
  grid-template-rows: auto 1fr auto;
  height: 100vh;

  .os-mac & {
    --app-upper-spacing: calc(2.5rem + 48px);
  }
`;