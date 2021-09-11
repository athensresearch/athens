import React from 'react';
import styled from 'styled-components';
import { DOMRoot } from '../../../config';

import { AddCircle } from '@material-ui/icons';
import { Modal, Popper } from '@material-ui/core';

import { Button } from '../../Button';
import { Overlay } from '../../Overlay';
import { Badge } from '../Badge';
import { Menu } from '../../Menu';

import { DatabaseIcon } from '../DatabaseIcon';
import { DatabaseMenuItem } from './components/DatabaseMenuItem';

const DatabaseMenuOverlay = styled(Overlay)`
  width: 16rem;
  max-height: 90vh;
  overflow-y: auto;

  svg {
      --size: 2em;
  }

  @supports (overflow-y: overlay) {
    overflow-y: overlay;
  }
`;

const ActiveDatabase = styled.div`
  display: flex;
  padding: 0.25rem 1rem 0.25rem 0.5rem;
  gap: 0.25rem;
  overflow: hidden;

  svg {
    flex: 0 0 2.5rem;
    margin-left: -0.325rem;
    --size: 2em;
  }

  main {
    display: flex;
    flex: 1 1 100%;
    /* Width is specified so that descendant text will get ellipses properly
    without resorting to overflow: hidden on this element. */
    width: calc(100% - 2rem);
    flex-direction: column;
    align-items: flex-start;
    justify-content: stretch;
  }

  h3 {
    margin: 0;
  }

  .tools {
    display: flex;
    margin-top: 0.5rem;
    font-size: var(--font-size--text-sm);

    button {
      padding: 0.25rem 0.5rem;
    }
  }
`;

const Name = styled.h4`
  font-weight: normal;
  font-size: var(--font-size--text-sm);
  color: inherit;
  margin: 0;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
`;

const Path = styled.span`
  display: block;
  color: var(--body-text-color---opacity-med);
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 12px;
  white-space: nowrap;
`;

export interface DatabaseMenuProps {
    activeDatabase: Database
    inactiveDatabases: Database[]
    isSynced: boolean
    handleChooseDatabase: (database: Database) => void
    handlePressAddDatabase: () => void
    handlePressRemoveDatabase: (database: Database) => void
    handlePressImportDatabase: (database: Database) => void
    handlePressMoveDatabase: (database: Database) => void
}

/**
 * Menu for switching to and joining graphs.
 */
export const DatabaseMenu = ({
    handlePressAddDatabase,
    handleChooseDatabase,
    handlePressImportDatabase,
    handlePressRemoveDatabase,
    handlePressMoveDatabase,
    activeDatabase,
    inactiveDatabases,
    isSynced
}: DatabaseMenuProps) => {
    const [isMenuOpen, setIsMenuOpen] = React.useState(false);
    const [menuAnchor, setMenuAnchor] = React.useState(null);

    const handlePressDatabaseMenu = (e) => {
        setMenuAnchor(e.currentTarget);
        setIsMenuOpen(true);
    };

    const handleCloseDatabaseMenu = () => {
        setMenuAnchor(null);
        setIsMenuOpen(false);
    };

    return (
        <>
            <Button
                isPressed={isMenuOpen}
                onClick={handlePressDatabaseMenu}
            >
                {isSynced ? (
                    <Badge><DatabaseIcon {...activeDatabase} /></Badge>
                ) : (
                    <DatabaseIcon {...activeDatabase} />
                )}
            </Button>
            <Modal
                onClose={handleCloseDatabaseMenu}
                BackdropProps={{ invisible: true }}
                container={DOMRoot}
                open={isMenuOpen}
            >
                <Popper
                    open={isMenuOpen}
                    anchorEl={menuAnchor}
                    disablePortal={true}
                    placement="bottom-start"
                >
                    <DatabaseMenuOverlay className="animate-in">
                        <Menu>
                            <ActiveDatabase>
                                <DatabaseIcon name={activeDatabase.name} />
                                <main>
                                    <Name>{activeDatabase.name}</Name>
                                    <Path>{activeDatabase.id}</Path>
                                    <div className="tools">
                                        {activeDatabase["is-remote"] ? (
                                            <>
                                                <Button onClick={(activeDatabase) => handlePressImportDatabase(activeDatabase)}>Import</Button>
                                                <Button onClick={(activeDatabase) => handlePressRemoveDatabase(activeDatabase)}>Remove</Button>
                                            </>
                                        ) : (
                                            <>
                                                <Button onClick={(activeDatabase) => handlePressMoveDatabase(activeDatabase)}>Import</Button>
                                                <Button onClick={(activeDatabase) => handlePressRemoveDatabase(activeDatabase)}>Remove</Button>
                                            </>
                                        )}
                                    </div>
                                </main>
                            </ActiveDatabase>
                            {inactiveDatabases.length > 0 && (
                                <>
                                    <Menu.Separator />
                                    {inactiveDatabases.map(database => <DatabaseMenuItem
                                        handleChooseDatabase={handleChooseDatabase}
                                        database={database}
                                        key={database.id} />)}
                                </>
                            )}
                            <Menu.Separator />
                            <Button onClick={handlePressAddDatabase}><AddCircle /><span>Add Database</span></Button>
                        </Menu>
                    </DatabaseMenuOverlay>
                </Popper>
            </Modal>
        </>
    )
}