import styled from 'styled-components';

export const Container = styled.div`
  display: flex;
  line-height: var(--line-height, 1.75em);
  position: relative;
  border-radius: 0.125rem;
  justify-content: flex-start;
  flex-direction: column;
  flex: 1 1 100%;
  color: inherit;

  &.show-tree-indicator:before {
    content: '';
    position: absolute;
    width: 1px;
    left: calc(1.375em + 1px);
    top: 2em;
    bottom: 0;
    transform: translateX(50%);
    transition: background-color 0.2s ease-in-out;
    background: var(--user-color, var(--border-color));
  }

  &.is-presence.show-tree-indicator:before {
    opacity: var(--opacity-low);
    transform: translateX(50%) scaleX(2);
  }

  &:after {
    content: '';
    position: absolute;
    top: 0.75px;
    right: 0;
    bottom: 0.75px;
    left: 0;
    opacity: 0;
    pointer-events: none;
    border-radius: 0.25rem;
    transition: opacity 0.075s ease;
    background: var(--link-color---opacity-lower);
  }

  &.is-selected:after {
    opacity: 1;
  }

  .is-selected &.is-selected {
    &:after {
      opacity: 0;
    }
  }

  .user-avatar {
    position: absolute;
    transition: transform 0.3s ease;
    left: 4px;
    top: 4px;
  }

  .block-body {
    display: grid;
    grid-template-areas: 'above above above above'
                        'toggle bullet content refs'
                        'below below below below';
    grid-template-columns: 1em 1em 1fr auto;
    grid-template-rows: 0 1fr 0;
    border-radius: 0.5rem;
    position: relative;

    .block-edit-toggle {
      position: absolute;
      appearance: none;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background: none;
      border: none;
      cursor: text;
      display: block;
      z-index: 1;
    }
  }

  .block-content {
    grid-area: content;
    min-height: 1.5em;

    &:hover + .user-avatar {
      transform: translateX(-2em);
    }
  }

  &.is-linked-ref {
    background-color: var(--background-plus-2);
  }

  /* Inset child blocks */
  & & {
    margin-left: var(--block-child-inset-margin, 2em);
    grid-area: body;
  }
`;
