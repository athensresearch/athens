/**
 * The ID of a unique Athens block
 */
type UID = string;

/**
 * A person interacting with Athens in a multiplayer context
 */
type Person = {
  personId: string;
  username: string;
  color: string;
}

/**
 *  A Person associated with a specific Athens block in a multiplayer context
*/
type PersonPresence = Person & {
  uid: UID;
}

/**
 *  A block
*/
type Block = {
  /**
   * The UID of this block
   */
  uid: UID;
  /**
   * Children
   */
  children?: any;
  /**
   * Whether this block's children should be shown
   */
  isOpen: boolean;
  /**
   * The raw content of the block
   */
  rawContent: string;
  /**
   * The rendered content of the block
   */
  renderedContent?: ReactNode;
  /**
   * Whether the block is part of a user selection
   */
  isSelected?: boolean;
  /**
   * Whether this block is locked
   */
  isLocked?: boolean;
  /**
   * Whether this block is editable
   */
  isEditable?: boolean;
  /**
   * A user attached to this block
   */
  presentUser?: PersonPresence;
}

/**
 * A knowledge graph
 */
type Database = {
  id: string;
  name: string;
  isRemote: boolean;
}

type Synced = boolean;
