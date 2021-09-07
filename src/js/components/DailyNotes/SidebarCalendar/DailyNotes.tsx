import React from 'react';
import styled from 'styled-components';
import { classnames } from '../../../utils/classnames';
import { dateFormat } from '../../../config';
import { DateTime, toISODate } from 'luxon';
import { Page, PageBlocksContainer } from '../../Page';
import { Button } from '../../Button';
import { BlockTree } from '../../Block/Block.stories';
import { useInView } from 'react-intersection-observer';
import DayPicker from 'react-day-picker';
import 'react-day-picker/lib/style.css';

const DailyNotesWrap = styled.div`
  display: flex;
`;

const DailyNotesPageWrap = styled.div`
  box-shadow: var(--depth-shadow-8);
  align-self: stretch;
  justify-self: stretch;
  margin: 1.25rem 2.5rem;
  padding: 1rem 2rem;
  transition-duration: 0s;
  border-radius: 0.5rem;

  &.is-today {
    background: var(--background-plus-1)
  }
`;

const Sidebar = styled.div`
  flex: 0 0 200px;
`;

const Pages = styled.div`
  min-height: calc(100vh + 1px);
  display: flex;
  padding: 1.25rem 0;
  align-items: 0;
  flex: 1 1 100%;
  flex-direction: column;
`;

/**
 * Current daily note, list of daily notes
 */


const DailyNotesPage = ({
  date,
  isToday,
  hasContent
}) => {
  const [isLinkedReferencesOpen, setIsLinkedReferencesOpen] = React.useState(true);
  const [isUnlinkedReferencesOpen, setIsUnlinkedReferencesOpen] = React.useState(true);
  const handlePressLinkedReferencesToggle = () => setIsLinkedReferencesOpen(!isLinkedReferencesOpen);
  const handlePressUnlinkedReferencesToggle = () => setIsUnlinkedReferencesOpen(!isUnlinkedReferencesOpen);

  const { ref, inView, entry } = useInView({
    threshold: 0,
  });

  return (
    <DailyNotesPageWrap
      ref={ref}
      className={classnames(
        inView ? "is-in-view" : "not-in-view",
        isToday ? "is-today" : "",
      )}>
      <Page
        title={date.plus({ days: 1 }).toLocaleString(dateFormat)}
        isDailyNote={true}
        uid={date.plus({ days: 1 }).toString()}
        handlePressRemoveShortcut={() => null}
        handlePressAddShortcut={() => null}
        handlePressShowLocalGraph={() => null}
        handlePressDelete={() => null}
        handlePressLinkedReferencesToggle={handlePressLinkedReferencesToggle}
        handlePressUnlinkedReferencesToggle={handlePressUnlinkedReferencesToggle}
        isLinkedReferencesOpen={isLinkedReferencesOpen}
        isUnlinkedReferencesOpen={isUnlinkedReferencesOpen}
        children={<PageBlocksContainer>{hasContent ? <BlockTree /> : <></>}</PageBlocksContainer>}
      />
    </DailyNotesPageWrap>
  );
};

const onDayClick = (day, modifiers, dayPickerInput) => { }

export const DailyNotes = ({ }) => {
  const [currentPage, setCurrentPage] = React.useState('today');

  const modifiers = {
    highlighted: new Date(2021, 9, 19),
  };

  return (
    <DailyNotesWrap>
      <Pages>
        <DailyNotesPage isToday={false} hasContent={false} date={DateTime.now().plus({ day: 1 })} />
        <DailyNotesPage isToday={true} hasContent={true} date={DateTime.now()} />
        <DailyNotesPage isToday={false} hasContent={false} date={DateTime.now().minus({ day: 1 })} />
      </Pages>
      <Sidebar>
        <DayPicker onDayClick={onDayClick} modifiers={modifiers} todayButton="today" />
      </Sidebar>
    </DailyNotesWrap>
  );
};