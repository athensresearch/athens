import React from "react";
import styled from "styled-components";

import { Item } from "./Item";
import { SkeletonItem } from "./SkeletonItem";

const Wrapper = styled.div`
  display: flex;
  flex-direction: column;
  align-items: stretch;
  grid-area: main;
  background: inherit;
  position: relative;
  z-index: 0;
  padding: 1rem;
`;

const Heading = styled.h2`
  font-size: var(--font-size--text-sm);
  letter-spacing: 0.03em;
  margin-top: 1rem;
  padding: 0.25rem calc(var(--main-padding-sides) + 0.375rem);
  color: var(--body-text-color---opacity-med);
  background: var(--background-color---opacity-high);
  position: sticky;
  top: 0rem;
  margin-left: 1px;
  margin-right: var(--main-padding-sides);
  backdrop-filter: blur(12px);
  border-top-right-radius: inherit;
  z-index: 9999;
`;

const Section = styled.section`
  display: flex;
  flex-direction: column;
  align-items: stretch;
  position: relative;
`;

const List = styled.ol`
  padding: 0 var(--main-padding-sides);
  margin: 0;
  list-style: none;
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 0.5rem;
`;

const Message = styled.div`
  border-radius: 1rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  place-content: center;
  background: var(--background-plus-2---opacity-10);
  z-index: 10;
  position: absolute;
  padding: 2rem 1rem;
  text-align: center;
  color: var(--body-text-color---opacity-med);
  backdrop-filter: blur(12px);
  width: 16rem;
  gap: 1rem;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);

  p {
    margin: 0;
    line-height: 1.4;
  }
`;

const TextButton = styled.button`
  display: inline;
  appearance: none;
  background: none;
  border: none;
  padding: 0;
  font-family: inherit;
  font-size: inherit;
  font-weight: inherit;
  line-height: inherit;
  color: var(--link-color);
  cursor: pointer;

  &:hover {
    text-decoration: underline;
  }
`;

const NoDatabasesMessage = ({ onNavToAdd, onNavToJoin }) =>
  <Message>
    <p>No graphs found.</p>
    <p>You probably want to <TextButton onClick={onNavToAdd}>Add</TextButton> or <TextButton onClick={onNavToJoin}>Join</TextButton> a graph.</p>
  </Message>;

const NoRecentDatabasesMessage = () =>
  <Message>
    <p>Create new graphs to keep notes completely separated.</p>
    <p>Join shared graphs to collaborate with your team.</p>
  </Message>;

interface DatabasesListProps {
  currentDatabase: Database;
  recentDatabases: Database[];
  onNavToAdd: () => void;
  onNavToJoin: () => void;
  onDeleteDatabase: (database: Database) => void;
  onRenameDatabase: (database: Database) => void;
  onChooseDatabase: (database: Database) => void;
  onRemoveDatabase: (database: Database) => void;
}

export const DatabasesList = React.forwardRef(
  (props: DatabasesListProps, ref): JSX.Element => {
    const {
      currentDatabase,
      recentDatabases,
      onNavToAdd,
      onNavToJoin,
      onDeleteDatabase: handleDeleteDatabase,
      onRenameDatabase: handleRenameDatabase,
      onRemoveDatabase: handleRemoveDatabase,
      onChooseDatabase: handleChooseDatabase,
    } = props;

    return (
      <Wrapper ref={ref}>
        {!currentDatabase && !recentDatabases?.length ? (
          <Section>
            <Heading></Heading>
            <List>
              <SkeletonItem key={1} />
              <SkeletonItem key={2} />
              <SkeletonItem key={3} />
              <SkeletonItem key={4} />
              <SkeletonItem key={5} />
              <SkeletonItem key={6} />
            </List>
            <NoDatabasesMessage
              onNavToAdd={onNavToAdd}
              onNavToJoin={onNavToJoin}
            />
          </Section>
        ) : (
          <>
            {currentDatabase && (
              <Section>
                <Heading>Current</Heading>
                <List>
                  <Item
                    isCurrentDatabase
                    db={currentDatabase}
                    onDeleteDatabase={handleDeleteDatabase}
                    onRenameDatabase={handleRenameDatabase}
                    onRemoveDatabase={handleRemoveDatabase}
                  />
                </List>
              </Section>
            )}
            {recentDatabases?.length > 0 ? (
              <Section>
                <Heading>Recent</Heading>
                <List>
                  {recentDatabases.map((db) => (
                    <Item
                      key={db.id}
                      db={db}
                      onChooseDatabase={handleChooseDatabase}
                      onDeleteDatabase={handleDeleteDatabase}
                      onRenameDatabase={handleRenameDatabase}
                      onRemoveDatabase={handleRemoveDatabase}
                    />
                  ))}
                </List>
              </Section>
            ) : (
              <Section>
                <Heading>Recent</Heading>
                <List>
                  <SkeletonItem key={1} />
                  <SkeletonItem key={2} />
                  <SkeletonItem key={3} />
                  <SkeletonItem key={4} />
                  <SkeletonItem key={5} />
                  <SkeletonItem key={6} />
                </List>
                <NoRecentDatabasesMessage />
              </Section>
            )}
          </>
        )}
      </Wrapper>
    );
  }
);
