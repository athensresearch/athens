import React from 'react';
import styled from 'styled-components';

import { ChevronRight } from '@material-ui/icons';

import { Button } from '../../../../Button';
import { blockTree } from '../../../Block/mockData';
import { renderBlocks } from '../../../Block/utils/renderBlocks';
import { Reference } from './components/Reference';


const Section = styled.section``;

const Count = styled.span`
  border-radius: 100em;
  background: var(--background-minus-2);
  color: var(--body-text-color---opacity-high);
  font-weight: bold;
  padding: 0.125rem 0.25rem;
  min-width: 1.25rem;
  font-size: var(--font-size--text-xs);
  text-align: center;
  margin-left: 0.5rem;
`;

const SectionHeading = styled.h4`
  font-weight: normal;
  display: flex;
  margin: 0;
  align-items: center;

  button {
    margin-right: 0.25rem;
    font-size: 1rem;
  }
`;

const GroupTitle = styled.h5`
  font-size: var(--font-size--text-lg);
  color: var(--link-color);
  margin: 0;
  font-weight: 500;

  a:hover {
    cursor: pointer;
    text-decoration: underline;
  }
`;

const ReferencesList = styled.div`
  font-size: 14px;
`;

const Group = styled.div`
  background: var(--background-minus-2---opacity-med);
  border-radius: 0.25rem;
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
`;

const GroupItems = styled.ol`
  display: contents;
`;

const SectionToggleButton = styled(Button).attrs(props => ({
  "aria-pressed": props.isOpen
}))`
  border-radius: 100em;
  padding: 0rem 0.5rem;
  margin-left: -0.5rem;
  
  &:hover {
    background: var(--background-minus-1);
  }
  padding: 0.1rem 0.5rem;

  &:active {
    background: var(--background-minus-2);
  }

  svg {
    transition: transform 0.1s ease-in-out;
    width: 0.75em;
    height: 0.75em;
    margin: -0.25rem 0.25rem -0.25rem -0.25rem;
  }

  &[aria-pressed="true"] {
    backdrop-filter: none;

    svg {
      transform: rotate(90deg);
    }
  }
`;

const ReferencesWrapper = styled.div`
  padding: 1rem 3rem;
  border-top: 1px solid var(--border-color);
  margin-top: 1rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
`;


const ReferenceSection = ({ label, isOpen, handlePressToggle, references }) => {
  return (<Section>
    <SectionToggleButton shape="round" variant="unset" isOpen={isOpen} onClick={handlePressToggle}>
      <SectionHeading>
        <ChevronRight /> {label} {!isOpen && (<Count>2</Count>)}
      </SectionHeading>
    </SectionToggleButton>
    {isOpen && (
      <ReferencesList>
        <Group>
          <GroupTitle><a>Athens UX Priorities</a></GroupTitle>
          <GroupItems>
            {renderBlocks({
              blockGraph: blockTree,
              blockComponent: <Reference />
            })}
          </GroupItems>
        </Group>
      </ReferencesList>)}
  </Section>)
}


export interface ReferencesProps {
  linkedRefs: any;
  unlinkedRefs: any;
  isLinkedReferencesOpen: boolean;
  isUnlinkedReferencesOpen: boolean;
  handlePressLinkedReferencesToggle: () => void;
  handlePressUnlinkedReferencesToggle: () => void;
}

export const References = ({
  linkedRefs,
  unlinkedRefs,
  isLinkedReferencesOpen,
  isUnlinkedReferencesOpen,
  handlePressLinkedReferencesToggle,
  handlePressUnlinkedReferencesToggle,
}) => {
  return (
    <ReferencesWrapper>
      <ReferenceSection
        label="Linked References"
        handlePressToggle={handlePressLinkedReferencesToggle}
        isOpen={isLinkedReferencesOpen}
        references={linkedRefs}
      />
      <ReferenceSection
        label="Unlinked References"
        handlePressToggle={handlePressUnlinkedReferencesToggle}
        isOpen={isUnlinkedReferencesOpen}
        references={unlinkedRefs}
      />
    </ReferencesWrapper>
  )
}