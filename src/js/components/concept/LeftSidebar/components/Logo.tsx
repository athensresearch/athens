import styled from 'styled-components';

export const Logo = styled.a`
  font-family: 'IBM Plex Serif';
  font-size: 18px;
  opacity: var(--opacity-med);
  letter-spacing: -0.05em;
  font-weight: bold;
  text-decoration: none;
  justify-self: flex-end;
  color: var(--header-text-color);
  transition: opacity 0.05s ease;

  &:hover {
    opacity: 1;
  }
`;
