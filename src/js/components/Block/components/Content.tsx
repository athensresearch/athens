import React, { ReactNode, useImperativeHandle } from 'react';
import styled from 'styled-components';

const ContentWrap = styled.div`
  grid-area: content;
  display: grid;
  grid-template-areas: "main";
  place-items: stretch;
  place-content: stretch;
  position: relative;
  overflow: visible;
  z-index: 2;
  flex-grow: 1;
  word-break: break-word;

  .rendered-content,
  textarea {
    grid-area: main;
    cursor: text;
    font-size: inherit;
    font-family: inherit;
    color: inherit;
  }

  textarea {
    color: inherit;
    font-size: inherit;
    position: relative;
    display: block;
    -webkit-appearance: none;
    resize: none;
    transform: translate3d(0,0,0);
    outline: none;
    background: transparent;
    caret-color: var(--link-color);
    min-height: 100%;
    padding: 0;
    margin: 0;
    border: 0;
    opacity: 0;
  }

  &.is-editing,
  &.show-editable-dom {
    textarea {
      z-index: 3;
      line-height: inherit;
      opacity: 0;
    }
  }

  &.is-editing {
    textarea {
      opacity: 1;
    }

    .rendered-content {
      opacity: 0;
    }
  }

  &:not(.is-editing):hover textarea {
    line-height: inherit;
  }

  .is-locked > .block-body > & {
    opacity: 0.5
  };

  span.text-run {
    pointer-events: none;

    > a {
      position: relative;
      z-index: 2;
      pointer-events: auto;
    }

  }

  span {
    grid-area: main;

    > span {
      > a {
        position: relative;
        z-index: 2;
      }
    }
  }

  abbr {
    grid-area: main;
    z-index: 4;

    > span {
      > a {
        position: relative;
        z-index: 2;
      }
    }
  }

  code, pre {
    font-family: 'IBM Plex Mono';
  }

  .media-16-9 {
    height: 0;
    width: calc(100% - 0.25rem);
    z-index: 1;
    transform-origin: right center;
    transition: all 0.2s ease;
    padding-bottom: calc(9 / 16 * 100%);
    margin-block: 0.25rem;
    margin-inline-end: 0.25rem;
    position: relative;
  }

  iframe {
    border: 0;
    box-shadow: inset 0 0 0 0.125rem var(background-minus-1);
    position: absolute;
    height: 100%;
    width: 100%;
    cursor: default;
    top: 0;
    right: 0;
    left: 0;
    bottom: 0;
    border-radius: 0.25rem;
  }

  img {
    border-radius: 0.25rem;
    max-width: calc(100% - 0.25rem);
  }

  h1, h2, h3, h4, h5, h6 {
    margin: 0;
    color: var(--body-text-color---opacity-higher);
    font-weight: 500;
  }

  h1 {
    padding: 0;
    margin-block-start: "-0.1em";
  }

  h2, h3 {
    padding: 0;
  }

  h4 {
    padding: 0.25em 0;
  }

  h5 {
    padding: 1em 0;
  }

  h6 {
    text-transform: uppercase;
    letter-spacing: 0.06em;
    padding: 1em 0;
  }

  p {
    margin: 0;
    padding-bottom: 1em;
  }

  blockquote {
    margin-block: 0.125rem;
    margin-inline: 0.5em;
    padding-block: calc(0.5em - 0.125rem - 0.125rem);
    padding-inline: 1.5em;
    border-radius: 0.25em;
    background: var(--background-minus-1);
    color: var(--body-text-color---opacity-high);

    p {
      padding-bottom: 1em;

      &:last-child {
        padding-bottom: 0;
      }
    }
  }

  mark.content-visibility.highlight {
    padding: 0 0.2em;
    border-radius: 0.125rem;
    background-color: var(--text-highlight-color);
  }

`;

export interface ContentProps {
  /**
   * The raw content of the block
   */
  rawContent: string;
  /**
   * The rendered content of the block
   */
  renderedContent?: ReactNode;
  /**
   * Whether the block is in editing mode
   */
  isEditable?: boolean;
  /**
   * Whether the block is in editing mode
   */
  isEditing?: boolean;
  /**
   * Whether the block has child blocks
   */
  isLocked?: boolean;
  /**
   * Whether the block should render its editable components or just the static content
   */
  showEditableDom?: boolean;
  /**
   * When raw content of a block is modified.
   * Returns the new value of the raw content.
   */
  handleContentChange?: (e: any) => void;
  /**
   * When the content is clicked or tapped
   */
  handlePressContent?: () => void;
}

export const Content = ({
  rawContent,
  renderedContent,
  isLocked,
  isEditable,
  isEditing,
  showEditableDom,
  handleContentChange,
  handlePressContent,
}: ContentProps) => (
  <ContentWrap
    className={[
      isLocked ? 'is-locked' : '',
      isEditing ? 'is-editing' : '',
      showEditableDom ? 'show-editable-dom' : '',
    ].join(' ')}
    onClick={handlePressContent}
  >
    {(isEditing || showEditableDom) && (<textarea
      rows={1}
      placeholder="Enter text"
      onKeyUp={handleContentChange}
      defaultValue={rawContent}
    />)
    }
    <div className="rendered-content">{renderedContent || rawContent}</div>
  </ContentWrap>
);
