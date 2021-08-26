import React, { ReactNode } from 'react';
import styled from 'styled-components';
import { Bullet } from './components/Bullet';
import { Body } from './components/Body';
import { Content } from './components/Content';
import { Toggle } from './components/Toggle';
import { Refs } from './components/Refs';

const Container = styled.div`
  border: 1px solid;
  display: flex;
  line-height: 2em;
  position: relative;
  border-radius: 0.125rem;
  justify-content: flex-start;
  flex-direction: column;

  &.show-tree-indicator:before {
    content: '';
    position: absolute;
    width: 1px;
    left: calc(1.375em + 1px);
    top: 2em;
    bottom: 0;
    transform: translateX(50%);
    transition: background-color 0.2s ease-in-out;
    background: var(--border-color);
  }

  &.is-presence.show-tree-indicator:before {
    background: var(--user-color);
  }

  &:after {
    content: '';
    z-index: -1;
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

    &.is-selected {
      opacity: 1;
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

  .block-container {
    margin-left: 2rem;
    grid-area: body;
  }


`;

export interface BlockProps {
  /**
   * Whether the block has child blocks
   */
  hasChildren?: boolean;
  /**
   * The raw text content of the block
   */
  content?: string;
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
  refsCount?: number;
  /**
   * Whether this block is expanded or not
   */
  isOpen: boolean;
  /**
   * The UID of this block
   */
  uid: string;
  /**
   * The UID of some other block
   */
  linkedRef: string;
  /**
   * Switches between isOpen and !isOpen
   */
  toggleIsBlockOpen: () => void;
}

export const Block: React.FC<BlockProps> = ({
  children,
  content,
  hasChildren,
  isOpen,
  linkedRef,
  refsCount,
  toggleIsBlockOpen = () => null,
  uid,
}) => (<Container>
  <div className="block-body">
    {hasChildren && <Toggle
      isOpen={isOpen}
      linkedRef={linkedRef}
      uid={uid}
      toggleIsBlockOpen={toggleIsBlockOpen}
    />
    }
    <Bullet isClosedWithChildren={!isOpen && hasChildren} />
    <Content content={content} />
    {refsCount && <Refs refsCount={refsCount} />}
  </div>
</Container>);