import React, { ReactNode } from 'react';
import styled from 'styled-components';
import { Create, ArrowForward } from '@material-ui/icons';

const ResultWrap = styled.li`
  display: flex;
  padding: 0.75rem 1.5rem;
  background: var(--background-plus-1);
  color: var(--body-text-color);
  cursor: default;
  gap: 0.5rem;
  transition: background 0.05s ease, color 0.05s ease;

  .title {
    font-size: 1rem;
    margin: 0;
    font-weight: 500;
    flex: 0 0 auto;
  }

  .preview {
    white-space: nowrap;
    opacity: var(--opacity-med);
    margin: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    flex: 1 1 100%;
    line-clamp: 1;
    display: inline-flex;
  }

  &.is-selected,
  &:hover {
    background: var(--link-color);
    color: #fff;
  }
`;

const LinkLeader = styled.span`
  margin-left: auto;
  height: 1em;
  opacity: 0;

  .is-selected > &,
  li:hover > & {
    opacity: 1;
  }
`;

export interface Result {
  /**
   * The title of the result.
   */
  title: React.ReactNode;
  /**
   * Truncated sample of the result's main text
   */
  preview?: React.ReactNode;
  /**
   * Whether the result is selected in the list of results
   */
  isSelected: boolean,
  /**
   * The type of action to take when choosing the result
  */
  action: 'create' | 'open'
  handleChooseResult: (result: Result) => void;
}

/**
 * The result of a search.
*/
export const Result = ({
  title,
  preview,
  isSelected,
  handleChooseResult,
  action
}: Result) =>
  <ResultWrap
    className={isSelected ? 'is-selected' : ''}
    onClick={handleChooseResult}
  >
    <h4 className="title">{title}</h4>
    {preview && <p className="preview">{preview}</p>}
    <LinkLeader>
      {action === 'create' ? <Create /> : <ArrowForward />}
    </LinkLeader>
  </ResultWrap>;
