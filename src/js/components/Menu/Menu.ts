import styled from 'styled-components';

import { Button } from '../Button';

/**
 * Wraps buttons into a menu.
 */
export const Menu = styled.div`
  display: grid;
  grid-gap: 0.125rem;
  min-width: 9em;
  align-items: stretch;
  grid-auto-flow: row;
`;

/**
 * Divider between sections of a menu.
 */
Menu.Separator = styled.hr`
  border: 0;
  background: var(--border-color);
  align-self: stretch;
  justify-self: stretch;
  height: 1px;
  margin: 0.25rem 0;
`;

/**
 * Wraps a menu item.
 */
Menu.Button = styled(Button)`
    justify-content: flex-start;
`;