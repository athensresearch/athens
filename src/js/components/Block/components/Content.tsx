import React, { ReactNode } from 'react';
import styled from 'styled-components';

const parseAndRender = (content: string): ReactNode => <span>{content}</span>

const ContentWrap = styled.div`
  display: grid;
  grid-area: content;
  grid-template-areas: 'main';
  align-items: stretch;
  justify-content: stretch;
  position: relative;
  overflow: visible;
  z-index: 2;
  flex-grow: 1;
  word-break: break-word;
`;

export interface ContentProps {
  /**
   * The raw content of the block
   */
  content: string;
  /**
   * Whether the block has child blocks
   */
  isLocked?: boolean;
  /**
   * Whether the block should render its editable components or just the static content
   */
  showEditableDom?: boolean;
}

export const Content = ({
  content,
  isLocked,
  showEditableDom,
}: ContentProps) => (
  <ContentWrap className={isLocked && 'is-locked'} >
    {showEditableDom ? <textarea>{content}</textarea>
      : parseAndRender(content)}
  </ContentWrap>
);
