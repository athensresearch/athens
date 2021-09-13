import styled from 'styled-components';

export const MainContent = styled.div`
  flex: 1 1 100%;
  margin-left: auto;
  margin-right: auto;
  grid-area: main-content;
  align-items: flex-start;
  justify-content: stretch;
  padding-top: var(--app-upper-spacing);
  display: flex;
  overflow-y: auto;

  @supports (overflow-y: overlay) {
    overflow-y: overlay;
  }

  &::-webkit-scrollbar {
    background: var(--background-minus-1);
    width: 0.5rem;
    height: 0.5rem;
  }

  &::-webkit-scrollbar-corner {
    background: var(--background-minus-1);
  }

  &::-webkit-scrollbar-thumb {
    background: var(--background-minus-2);
  }
`;