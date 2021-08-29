import styled from 'styled-components';

export const ShortcutsList = styled.ol`
  flex: 1 1 100%;
  display: flex;
  list-style: none;
  flex-direction: column;
  padding: 0 2rem;
  margin: 0 0 2rem;
  overflow-y: auto;

  @supports (overflow-y: overlay) {
    overflow-y: overlay;
  }

  .heading {
    flex: 0 0 auto;
    opacity: var(--opacity-med);
    line-height: 1;
    margin: 0 0 0.25rem;
    font-size: inherit;
  }
`;
