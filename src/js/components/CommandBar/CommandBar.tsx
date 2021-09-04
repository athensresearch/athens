import React from 'react';
import styled from 'styled-components';

import { DOMRoot } from '../../config';

import { Close } from '@material-ui/icons';
import { Modal } from '@material-ui/core';
import { Result } from './components/Result';

const Container = styled.div`
  width: 49rem;
  display: flex;
  background: var(--background-plus-1);
  overflow: hidden;
  box-shadow: var(--depth-shadow-64), 0 0 0 1px var(--shadow-color---opacity-lower);
  max-width: calc(100vw - 1rem);
  border-radius: 0.25rem;
  flex-direction: column;
  max-height: 60vh;
  margin: 4rem auto;
  position: relative;
`;

const Input = styled.input`
  background: var(--background-plus-2);
  width: 100%;
  border: 0;
  padding: 1.5rem 4rem 1.5rem 1.5rem;
  font-size: 2.375rem;
  font-weight: 300;
  line-height: 1.3;
  letter-spacing: -0.03em;
  border-radius: 0.25rem 0.25rem 0 0;
  color: var(--body-text-color);
  caret-color: var(--link-color);
  cursor: text;
  font-family: inherit;

  &:focus {
    outline: none;
  }

  &::placeholder {
    color: var(--body-text-color---opacity-low);
  }

  &::-webkit-search-cancel-button {
    display: none;
  }
`;

const Header = styled.header`
  position: relative;
`;

const SearchCancelButton = styled.button`
  background: none;
  color: inherit;
  position: absolute;
  transition: opacity 0.1s ease, background 0.1s ease;
  cursor: pointer;
  border: 0;
  right: 1rem;
  place-items: center;
  place-content: center;
  height: 2.5rem;
  width: 2.5rem;
  border-radius: 1000px;
  display: flex;
  transform: translate(0%, -50%);
  top: 50%;

  &:hover,
  &:focus {
    background: var(--background-plus-1);
  }
`;

const ResultsList = styled.ol`
  background: var(--background-color);
  overflow-y: auto;
  max-height: 100%;
  padding: 0;
  list-style: none;
  margin: 0;
`;

const ResultsHeading = styled.header`
  padding: 0.25rem 1.5rem;
  background: var(--background-plus-2);
  display: flex;
  position: sticky;
  margin: 0;
  flex-wrap: wrap;
  gap: 0.5rem;
  align-items: center;
  top: 0;
  justify-content: space-between;
  box-shadow: 0 1px 0 0 var(--border-color);
  border-top: 1px solid var(--border-color);
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.03em;
  font-size: 0.75em;
`;

const Hint = styled.span`
  color: inherit;
  letter-spacing: auto;
  font-size: unset;
  text-transform: none;
  opacity: var(--opacity-med);
  font-weight: 500;

  kbd {
    font-family: inherit;
    background: var(--background-color);
    border-radius: 0.25rem;
    color: var(--body-text-color);
    font-size: 85%;
    font-weight: 600;
    padding: 0.125rem 0.5rem;
    text-transform: uppercase;
    letter-spacing: 0.03em;
  }
`;

export interface CommandBarProps {
  /**
   * Items returned based on the search query
   */
  results?: Result[];
  /**
   * Prefill the search input with a query
  */
  defaultQuery?: string;
  /**
   * Whether the search input field should be autofocused
  */
  autoFocus?: true;
  /**
   * Whether to render the command bar portaled modal.
   * If false, the command bar will be rendered in place.
   */
  isModal?: boolean;
  /**
  * Element to attach Portal to
  **/
  container?: Element;
  handleChooseResult?(result: Result): void;
  handleQueryChange?(query: string): void;
  handlePressQueryClear?(): void;
  handleCloseCommandBar?(): void;
}

/**
 * Finds and creates pages. Command bar.
 */
export const CommandBar = ({
  results,
  defaultQuery,
  autoFocus,
  isModal = true,
  container,
  handleChooseResult,
  handlePressQueryClear,
  handleQueryChange,
  handleCloseCommandBar,
}: CommandBarProps) => {
  const [query, setQuery] = React.useState(defaultQuery);

  const content = (
    <Container>
      <Header>
        <Input
          type="search"
          id="athena-input"
          autoFocus={autoFocus}
          autocomplete="off"
          suggestions="off"
          spellcheck={false}
          required={true}
          placeholder="Find or Create Page"
          onChange={(e) => handleQueryChange(e.target.value)}
          onKeyDown={(e) => setQuery(e.target.value)}
          defaultValue={query || defaultQuery}
        />
        <SearchCancelButton onClick={handlePressQueryClear}>
          <Close />
        </SearchCancelButton>
      </Header>
      {query && (
        <ResultsList>
          <ResultsHeading>
            <span>{results ? results.length : 0} Results</span>
            <Hint>Press <kbd>shift + enter</kbd> to open in sidebar</Hint>
          </ResultsHeading>
          {results ? results.map((result) => (
            <Result
              {...result}
              key={result.title.toString() + result.preview.toString()}
            />
          )) : (
            <Result
              title={<><b>Create Page:</b> {query}</>}
              handleChooseResult={handleChooseResult}
              isSelected={true}
              action="create"
            />
          )}
        </ResultsList>
      )}
    </Container>
  )

  return isModal ? (
    <Modal
      open={true}
      disablePortal={true}
      container={container ? container : DOMRoot}
      onClose={handleCloseCommandBar}
      BackdropProps={{ invisible: true }}
    >
      {content}
    </Modal>
  ) : (content)

}

