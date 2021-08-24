import React, { ReactNode } from 'react';
import styled from 'styled-components';
import { Body } from './components/Body';
import { Content } from './components/Content';

const Container = styled.div`
  border: 1px solid;
  display: flex;
  line-height: 2em;
  position: relative;
  border-radius: 0.125rem;
  justify-content: flex-start;
  flex-direction: column;
`;

const Toggle = styled.button`
  grid-area: toggle;
`;
const Bullet = styled.button`
  grid-area: bullet;
  flex-shrink: 0;
  grid-area: bullet;
  position: relative;
  z-index: 2;
  cursor: pointer;
  margin-right: 0.25em;
  appearance: none;
  border: 0;
  background: transparent;
  transition: all 0.05s ease;
  height: 2em;
  width: 1em;

  &:after {
    content: '';
    background: currentColor;
    transition: color 0.05s ease, opacity 0.05s ease, box-shadow 0.05s ease, transform 0.05s ease;
    border-radius: 100px;
    box-shadow: 0 0 0 0.125rem transparent;
    display: inline-flex;
    margin: 50% 0 0 50%;
    transform: translate(-50%, -50%);
    height: 0.3125em;
    width: 0.3125em;
  }

  &:before {
    content: '';
    inset: 0.25rem -0.125rem;
    z-index: -1;
    transition: opacity 0.1s ease;
    position: absolute;
    border-radius: 0.25rem;
    opacity: 0;
    /* box-shadow: (:4 style/DEPTH-SHADOWS); */
    background: var(--background-plus-2);
  }

  &:hover {
    color: var(--link-color);
  }

  &:hover,
  &:hover:before,
  &:focus-visible:before {
    opacity: 1;
  }

  &:hover:after {
    transform: translate(-50%, -50%) scale(1.3);
  }

  &.dragging {
    z-index: 1;
    cursor: grabbing;
    color: var(--body-text-color);
  }

  &.closed-with-children {
    &:after {
      box-shadow: 0 0 0 0.125rem var(--body-text-color);
      // opacity: var(--opacity-med);
    }
  }
`;
const RefsCount = styled.button`
  grid-area: refs;
`;
const Presence = styled.ol``;
const Attributes = styled.aside``;

export interface BlockProps {
  /**
   * Whether the block has child blocks
   */
  hasChildren?: boolean;
  /**
   * Whether the block is part of a user selection
   */
  isSelected?: boolean;
  /**
   * Children
   */
  children?: ReactNode;
  /**
   * Number of references to this block
   */
  refsCount?: ReactNode;
  /**
   * Whether this block is expanded or not
   */
  isOpen: boolean;
}

export const Block: React.FC<BlockProps> = ({
  children,
  hasChildren,
  refsCount,
  isOpen
}) => (<Container>
  <Body>
    {hasChildren && <Toggle children={'V'} />}
    <Bullet />
    <Content>{children}</Content>
    {refsCount && <RefsCount />}
  </Body>
</Container>);