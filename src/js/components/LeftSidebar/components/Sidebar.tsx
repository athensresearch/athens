import styled from 'styled-components';

export const Sidebar = styled.section`
  width: 0;
  grid-area: left-sidebar;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow-x: hidden;
  overflow-y: auto;
  transition: width 0.5s ease;

  @supports (overflow-y: overlay) {
    overflow-y: overlay;
  }

  .top-line {
    margin-bottom: 2.5rem;
    display: flex;
    flex: 0 0 auto;
    justify-content: space-between;
  }

  .footer {
    flex: 0 0 auto;
    margin: auto 2rem 0;
    align-self: stretch;
    align-items: baseline;
    display: flex;
    grid-gap: 0.25rem;
  }

  .small-icon {
    font-size: 16px;
  }
  .large-icon {
    font-size: 22px;
  }

  .container {
    width: 18rem;
    height: 100%;
    display: flex;
    flex-direction: column;
    padding: calc(var(--app-upper-spacing) + 2rem) 0 1rem;
    transition: opacity 0.5s ease;

    .is-left-sidebar-open & {
      opacity: 1;
    } 

    .is-left-sidebar-closed & {
      opacity: 0;
    } 

  }

  &.is-left-sidebar-open {
    width: 18rem;
  }

  &.is-left-sidebar-closed {
    width: 0;
  }
`;
