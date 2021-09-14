import styled from 'styled-components';

export const Version = styled.a`
  display: block;
  font-size: 0.75em;
  font-weight: 500;
  line-height: 1;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  margin: 0.2em 0 0.2em auto;
  color: var(--header-text-color);
  transition: opacity 0.08s ease-in-out;
  text-decoration: none;
  opacity: 0.3;
  font-size: clamp(12px, 100%, 14px);

  &:hover {
    opacity: 1;
  }
`;
