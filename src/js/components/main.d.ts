/**
 * The ID of a unique Athens block
 */
type UID = string | number;

/**
 * A person interacting with Athens in a multiplayer context
 */
type Person = {
  personId: string;
  username: string;
  color: string; // CSS color
}

/**
 * OS the name of supported OSs
 */
type OS = 'mac' | 'windows' | 'linux';

/**
 * The state of a session's connection to the Athens host
 */
type ConnectionStatus = 'local' | 'connecting' | 'connected' | 'reconnecting' | 'offline';

type HostAddress = string;

/**
 * A knowledge graph
 */
type Database = {
  id: string;
  name: string;
  isRemote: boolean;
  icon?: string; // Emoji
  color?: string; // CSS color
}

type Synced = boolean;


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
  renderedContent?: any;
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
   * Whether this block represents a checked checkbox
   */
  isChecked?: boolean;
  /**
   * A user attached to this block
   */
  presentUser?: PersonPresence;
}

type BlockGraph = {
  tree: any[],
  blocks: any,
};