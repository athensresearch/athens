import styled from 'styled-components';

import { Button } from '@/Button';

/**
 * Wraps buttons into a menu.
 */
export const Menu = styled.div`
  display: flex;
  gap: 0.125rem;
  min-width: 9em;
  align-items: stretch;
  flex-direction: column;
  &:focus {
    outline: none;
  }
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
  flex: 0 0 auto;
`;

/**
 * Wraps a menu item.
 */
Menu.Button = styled(Button).attrs({
  shape: 'unset'
})`
  font-size: var(--font-size--text-sm);
  flex: 0 0 auto;
  justify-content: flex-start;
  padding: 0.125rem 0.5rem;
  border-radius: 0.25rem;
`;

/**
 * Heading for a section of a menu.
 */
Menu.Heading = styled.h3`
  margin: 0;
  font-weight: 500;
  flex: 1 1 100%;
  padding: 0.25rem 0.5rem;
  font-size: var(--font-size--text-sm);
  color: var(--body-text-color---opacity-med);
`;