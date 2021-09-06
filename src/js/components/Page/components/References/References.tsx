import React from 'react';
import styled from 'styled-components';

import { ChevronRight } from '@material-ui/icons';

import { Button } from '../../../Button';
import { blockTree } from '../../../Block/mockData';
import { recurseBlocks } from '../../../../utils/recurseBlocks';
import { Reference } from './components/Reference';


const Section = styled.section`
  margin-top: 3em;
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
  padding-left: 3rem;
  padding-right: 3rem;
`;


const ReferenceSection = ({ label, isOpen, handlePressToggle, references }) => {
  return (<Section>
    <SectionToggleButton shape="round" variant="unset" isOpen={isOpen} onClick={handlePressToggle}>
      <SectionHeading>
        <ChevronRight /> {label}
      </SectionHeading>
    </SectionToggleButton>
    {isOpen && (
      <ReferencesList>
        <Group>
          <GroupTitle><a>Athens UX Priorities</a></GroupTitle>
          <GroupItems>
            {recurseBlocks({
              tree: blockTree.tree,
              content: blockTree.blocks,
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
  handlePressUninkedReferencesToggle: () => void;
}

export const References = ({
  linkedRefs,
  unlinkedRefs,
  isLinkedReferencesOpen,
  handlePressLinkedToggle,
  isUnlinkedReferencesOpen,
  handlePressUnlinkedToggle,
}) => {
  return (
    <ReferencesWrapper>
      <ReferenceSection
        label="Linked References"
        handlePressToggle={handlePressLinkedToggle}
        isOpen={isLinkedReferencesOpen}
        references={linkedRefs}
      />
      <ReferenceSection
        label="Unlinked References"
        handlePressToggle={handlePressUnlinkedToggle}
        isOpen={isUnlinkedReferencesOpen}
        references={unlinkedRefs}
      />
    </ReferencesWrapper>
  )
}