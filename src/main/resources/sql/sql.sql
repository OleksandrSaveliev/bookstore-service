-- H2 SCHEMA + SEED DATA
CREATE TABLE IF NOT EXISTS users (
                                     id       BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     email    VARCHAR(255) NOT NULL UNIQUE,
                                     password VARCHAR(255) NOT NULL,
                                     role     VARCHAR(20)  NOT NULL
);

CREATE TABLE IF NOT EXISTS client_profiles (
                                               id      BIGINT AUTO_INCREMENT PRIMARY KEY,
                                               user_id BIGINT         NOT NULL UNIQUE,
                                               name    VARCHAR(255)   NOT NULL,
                                               balance DECIMAL(19, 2) NOT NULL,
                                               CONSTRAINT fk_client_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS employee_profiles (
                                                 id         BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                 user_id    BIGINT       NOT NULL UNIQUE,
                                                 name       VARCHAR(255),
                                                 phone      VARCHAR(50),
                                                 birth_date DATE,
                                                 CONSTRAINT fk_employee_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS books (
                                     id               BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     name             VARCHAR(255)   NOT NULL UNIQUE,
                                     genre            VARCHAR(100),
                                     age_group        VARCHAR(20),
                                     price            DECIMAL(19, 2),
                                     publication_year DATE,
                                     author           VARCHAR(255),
                                     number_of_pages  INT,
                                     stock            INT,
                                     characteristics  VARCHAR(500),
                                     description      TEXT,
                                     language         VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS orders (
                                      id         BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      client_id  BIGINT,
                                      order_date DATETIME(6),
                                      price      DECIMAL(19, 2),
                                      status     VARCHAR(20) NOT NULL,
                                      created_at DATETIME,
                                      updated_at DATETIME,
                                      CONSTRAINT fk_orders_client FOREIGN KEY (client_id) REFERENCES client_profiles (id)
);

CREATE TABLE IF NOT EXISTS book_items (
                                          id       BIGINT AUTO_INCREMENT PRIMARY KEY,
                                          order_id BIGINT,
                                          book_id  BIGINT,
                                          quantity INT,
                                          CONSTRAINT fk_items_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
                                          CONSTRAINT fk_items_book  FOREIGN KEY (book_id)  REFERENCES books (id)
);

CREATE INDEX IF NOT EXISTS idx_orders_status   ON orders(status);
CREATE INDEX IF NOT EXISTS idx_book_items_order ON book_items(order_id);

-- Password for all users: password123

INSERT INTO users (email, password, role) VALUES
                                              ('employee@bookstore.com', '$2a$10$wvpSaiTftBQRPe1VerIey.Nx5TnclSEouhQN35O4.9WqAcEiefVxu', 'EMPLOYEE'),
                                              ('client@bookstore.com',   '$2a$10$wvpSaiTftBQRPe1VerIey.Nx5TnclSEouhQN35O4.9WqAcEiefVxu', 'CLIENT'),
                                              ('admin@bookstore.com',    '$2a$10$wvpSaiTftBQRPe1VerIey.Nx5TnclSEouhQN35O4.9WqAcEiefVxu', 'ADMIN');

INSERT INTO employee_profiles (user_id, name, phone, birth_date)
SELECT id, 'Alice Johnson', '+380991234567', '1990-05-15' FROM users WHERE email = 'employee@bookstore.com';

INSERT INTO employee_profiles (user_id, name, phone, birth_date)
SELECT id, 'System Administrator', '+380440000000', '1985-01-01' FROM users WHERE email = 'admin@bookstore.com';

INSERT INTO client_profiles (user_id, name, balance)
SELECT id, 'Bob Smith', 500.00 FROM users WHERE email = 'client@bookstore.com';

INSERT INTO books (name, genre, age_group, price, publication_year, author, number_of_pages, stock, characteristics, description, language) VALUES
                                                                                                                                                ('The Great Gatsby',                        'Fiction',         'ADULT', 12.99, '1925-04-10', 'F. Scott Fitzgerald',      180,  25, 'Classic American novel',           'A story of wealth and obsession in the Jazz Age.',                                          'ENGLISH'),
                                                                                                                                                ('To Kill a Mockingbird',                   'Fiction',         'TEEN',  14.99, '1960-07-11', 'Harper Lee',               281,  30, 'Pulitzer Prize winner',            'A powerful story of racial injustice in the American South.',                               'ENGLISH'),
                                                                                                                                                ('1984',                                    'Dystopia',        'ADULT', 13.99, '1949-06-08', 'George Orwell',            328,  40, 'Political dystopia classic',        'A chilling vision of a totalitarian surveillance state.',                                   'ENGLISH'),
                                                                                                                                                ('Brave New World',                         'Dystopia',        'ADULT', 13.49, '1932-01-01', 'Aldous Huxley',            311,  35, 'Philosophical sci-fi',              'A world of pleasure and control without freedom.',                                          'ENGLISH'),

                                                                                                                                                ('Harry Potter and the Sorcerer''s Stone',  'Fantasy',         'CHILD', 16.99, '1997-06-26', 'J.K. Rowling',             309,  50, 'Worldwide bestseller',              'A young boy discovers he is a wizard and enters a magical world.',                          'ENGLISH'),
                                                                                                                                                ('The Hobbit',                              'Fantasy',         'TEEN',  15.99, '1937-09-21', 'J.R.R. Tolkien',           310,  45, 'Epic fantasy classic',              'Bilbo Baggins goes on an unexpected adventure.',                                            'ENGLISH'),
                                                                                                                                                ('The Lord of the Rings',                   'Fantasy',         'ADULT', 29.99, '1954-07-29', 'J.R.R. Tolkien',           1178, 20, 'Epic fantasy trilogy',              'The definitive quest to destroy the One Ring.',                                             'ENGLISH'),
                                                                                                                                                ('Pride and Prejudice',                     'Romance',         'ADULT', 11.99, '1813-01-28', 'Jane Austen',              432,  28, 'Classic romance',                   'Elizabeth Bennet navigates love and society in Regency England.',                           'ENGLISH'),
                                                                                                                                                ('Jane Eyre',                               'Romance',         'ADULT', 12.49, '1847-10-16', 'Charlotte Bronte',         532,  22, 'Victorian classic',                 'An orphaned girl finds love and independence.',                                             'ENGLISH'),
                                                                                                                                                ('Wuthering Heights',                       'Romance',         'ADULT', 11.49, '1847-12-01', 'Emily Bronte',             464,  18, 'Gothic romance',                    'A tale of passion and revenge on the Yorkshire moors.',                                     'ENGLISH'),
                                                                                                                                                ('The Catcher in the Rye',                  'Fiction',         'TEEN',  13.99, '1951-07-16', 'J.D. Salinger',            277,  33, 'Coming-of-age classic',             'Holden Caulfield wanders New York after expulsion from school.',                           'ENGLISH'),
                                                                                                                                                ('Of Mice and Men',                         'Fiction',         'TEEN',  10.99, '1937-02-06', 'John Steinbeck',           112,  27, 'American classic',                  'Two migrant workers dream of a better life during the Depression.',                        'ENGLISH'),
                                                                                                                                                ('The Alchemist',                           'Philosophy',      'ADULT', 14.99, '1988-01-01', 'Paulo Coelho',             197,  60, 'Inspirational bestseller',          'A shepherd boy travels in search of treasure and his destiny.',                            'ENGLISH'),
                                                                                                                                                ('Don Quixote',                             'Adventure',       'ADULT', 18.99, '1605-01-16', 'Miguel de Cervantes',      1072, 15, 'First modern novel',               'A man driven mad by chivalric romances sets off as a knight.',                             'SPANISH'),
                                                                                                                                                ('One Hundred Years of Solitude',           'Magic Realism',   'ADULT', 16.49, '1967-05-30', 'Gabriel Garcia Marquez',   417,  25, 'Nobel Prize masterpiece',           'Seven generations of the Buendia family in the mythical Macondo.',                        'SPANISH'),
                                                                                                                                                ('Crime and Punishment',                    'Psychological',   'ADULT', 15.99, '1866-01-01', 'Fyodor Dostoevsky',        671,  20, 'Russian classic',                   'A student commits murder and grapples with guilt.',                                        'ENGLISH'),
                                                                                                                                                ('The Brothers Karamazov',                  'Philosophical',   'ADULT', 17.99, '1880-01-01', 'Fyodor Dostoevsky',        796,  15, 'Russian classic',                   'A passionate philosophical novel about faith, doubt, and family.',                         'ENGLISH'),
                                                                                                                                                ('War and Peace',                           'Historical',      'ADULT', 22.99, '1869-01-01', 'Leo Tolstoy',              1392, 10, 'Epic historical novel',             'Russian society during the Napoleonic Wars.',                                              'ENGLISH'),
                                                                                                                                                ('Anna Karenina',                           'Romance',         'ADULT', 16.99, '1878-01-01', 'Leo Tolstoy',              964,  18, 'Russian classic',                   'A married aristocrat falls into a tragic love affair.',                                    'ENGLISH'),
                                                                                                                                                ('The Little Prince',                       'Fantasy',         'CHILD',  9.99, '1943-04-06', 'Antoine de Saint-Exupery', 96,   70, 'Beloved children''s tale',         'A pilot stranded in the desert meets a little prince from another planet.',               'FRENCH'),
                                                                                                                                                ('Les Miserables',                          'Historical',      'ADULT', 19.99, '1862-01-01', 'Victor Hugo',              1463, 12, 'French classic',                    'The struggles of ex-convict Jean Valjean in 19th century France.',                        'FRENCH'),
                                                                                                                                                ('The Count of Monte Cristo',               'Adventure',       'ADULT', 20.99, '1844-01-01', 'Alexandre Dumas',          1276, 14, 'Classic adventure',                 'A wrongly imprisoned man escapes and plots his revenge.',                                  'FRENCH'),
                                                                                                                                                ('Faust',                                   'Drama',           'ADULT', 14.49, '1808-01-01', 'Johann Wolfgang von Goethe', 512, 10, 'German literary classic',          'A scholar sells his soul to the devil in pursuit of knowledge.',                           'GERMAN'),
                                                                                                                                                ('The Trial',                               'Philosophical',   'ADULT', 13.49, '1925-01-01', 'Franz Kafka',              255,  22, 'Absurdist classic',                 'A man is arrested and prosecuted by an inaccessible authority.',                           'GERMAN'),
                                                                                                                                                ('Kafka on the Shore',                      'Magic Realism',   'ADULT', 15.99, '2002-09-12', 'Haruki Murakami',          505,  30, 'Japanese contemporary',             'Two parallel stories weave through surreal events in Japan.',                              'JAPANESE'),
                                                                                                                                                ('Norwegian Wood',                          'Romance',         'ADULT', 14.49, '1987-09-04', 'Haruki Murakami',          296,  35, 'Japanese romance',                  'A nostalgic story of love and loss in 1960s Tokyo.',                                       'JAPANESE'),
                                                                                                                                                ('Kokoro',                                  'Psychological',   'ADULT', 12.99, '1914-01-01', 'Natsume Soseki',           248,  16, 'Japanese classic',                  'An exploration of loneliness and the Meiji era in Japan.',                                'JAPANESE'),
                                                                                                                                                ('Kobzar',                                  'Poetry',          'ADULT', 11.99, '1840-01-01', 'Taras Shevchenko',         304,  40, 'Ukrainian literary treasure',        'The foundational poetry collection of Ukrainian literature.',                              'UKRAINIAN'),
                                                                                                                                                ('Tini zabutykh predkiv',                   'Historical',      'TEEN',  10.99, '1911-01-01', 'Mykhailo Kotsiubynsky',    128,  25, 'Ukrainian classic',                 'A tragic love story set in the Carpathian mountains.',                                    'UKRAINIAN'),
                                                                                                                                                ('Lisova pisnia',                           'Drama',           'TEEN',  10.49, '1911-01-25', 'Lesia Ukrainka',           112,  22, 'Ukrainian dramatic poem',           'A poetic drama blending folklore and mythology.',                                          'UKRAINIAN'),
                                                                                                                                                ('Dune',                                    'Sci-Fi',          'ADULT', 17.99, '1965-08-01', 'Frank Herbert',            688,  38, 'Epic science fiction',              'A desert planet holds the universe''s most precious resource.',                            'ENGLISH'),
                                                                                                                                                ('Foundation',                              'Sci-Fi',          'ADULT', 15.99, '1951-05-01', 'Isaac Asimov',             255,  30, 'Classic sci-fi',                    'A mathematician predicts the fall of civilization and plans its recovery.',               'ENGLISH'),
                                                                                                                                                ('Ender''s Game',                           'Sci-Fi',          'TEEN',  14.99, '1985-01-15', 'Orson Scott Card',         352,  32, 'Military sci-fi',                   'A child prodigy is trained to fight an alien war.',                                        'ENGLISH'),
                                                                                                                                                ('The Hitchhiker''s Guide to the Galaxy',   'Sci-Fi',          'ADULT', 13.99, '1979-10-12', 'Douglas Adams',            193,  45, 'Comic sci-fi classic',              'An ordinary man is swept across the galaxy after Earth is demolished.',                    'ENGLISH'),
                                                                                                                                                ('Neuromancer',                             'Sci-Fi',          'ADULT', 14.49, '1984-07-01', 'William Gibson',           271,  20, 'Cyberpunk classic',                 'A washed-up hacker is hired for one last job in cyberspace.',                              'ENGLISH'),
                                                                                                                                                ('The Road',                                'Post-Apocalyptic','ADULT', 13.99, '2006-09-26', 'Cormac McCarthy',          287,  25, 'Pulitzer Prize winner',             'A father and son journey through a post-apocalyptic America.',                            'ENGLISH'),
                                                                                                                                                ('No Country for Old Men',                  'Thriller',        'ADULT', 13.49, '2005-07-19', 'Cormac McCarthy',          309,  20, 'Neo-Western thriller',              'A hunter stumbles upon drug money and a relentless killer.',                              'ENGLISH'),
                                                                                                                                                ('Gone Girl',                               'Thriller',        'ADULT', 14.99, '2012-06-05', 'Gillian Flynn',            422,  35, 'Psychological thriller',            'A woman disappears on her anniversary and nothing is as it seems.',                       'ENGLISH'),
                                                                                                                                                ('The Girl with the Dragon Tattoo',         'Mystery',         'ADULT', 15.49, '2005-08-01', 'Stieg Larsson',            672,  28, 'Nordic noir',                       'A journalist and hacker investigate a decades-old disappearance.',                        'ENGLISH'),
                                                                                                                                                ('Sherlock Holmes: Complete',               'Mystery',         'ADULT', 19.99, '1892-10-14', 'Arthur Conan Doyle',       1122, 20, 'Classic detective fiction',         'The complete adventures of the world''s greatest detective.',                              'ENGLISH'),
                                                                                                                                                ('And Then There Were None',                'Mystery',         'ADULT', 12.99, '1939-11-06', 'Agatha Christie',          264,  40, 'Best-selling mystery',              'Ten strangers are lured to an island and begin to die one by one.',                       'ENGLISH'),
                                                                                                                                                ('The Da Vinci Code',                       'Thriller',        'ADULT', 14.99, '2003-03-18', 'Dan Brown',                689,  42, 'Mystery thriller',                  'A symbologist uncovers a religious conspiracy hidden for centuries.',                      'ENGLISH'),
                                                                                                                                                ('Sapiens',                                 'Non-Fiction',     'ADULT', 18.99, '2011-01-01', 'Yuval Noah Harari',        443,  50, 'History of humankind',              'A sweeping history of the human species from Stone Age to present.',                      'ENGLISH'),
                                                                                                                                                ('Homo Deus',                               'Non-Fiction',     'ADULT', 17.99, '2015-09-04', 'Yuval Noah Harari',        450,  35, 'Future of humanity',               'What will become of humanity in the age of algorithms and biotech?',                      'ENGLISH'),
                                                                                                                                                ('Atomic Habits',                           'Self-Help',       'ADULT', 16.99, '2018-10-16', 'James Clear',              320,  55, 'Practical self-improvement',        'Tiny changes that lead to remarkable results.',                                            'ENGLISH'),
                                                                                                                                                ('The Power of Habit',                      'Self-Help',       'ADULT', 15.99, '2012-02-28', 'Charles Duhigg',           371,  40, 'Habit science',                     'Why we do what we do in life and business.',                                               'ENGLISH'),
                                                                                                                                                ('Thinking, Fast and Slow',                 'Psychology',      'ADULT', 17.49, '2011-10-25', 'Daniel Kahneman',          499,  30, 'Behavioral economics',              'How two systems of thinking shape our judgments and decisions.',                           'ENGLISH'),
                                                                                                                                                ('The Subtle Art of Not Giving a F*ck',     'Self-Help',       'ADULT', 15.99, '2016-09-13', 'Mark Manson',              224,  48, 'Counterintuitive self-help',        'A refreshing antidote to the cult of positivity.',                                        'ENGLISH'),
                                                                                                                                                ('Charlotte''s Web',                        'Fiction',         'CHILD',  8.99, '1952-10-15', 'E.B. White',               184,  60, 'Beloved children''s classic',       'A spider saves her friend Wilbur the pig from slaughter.',                                'ENGLISH'),
                                                                                                                                                ('The Lion, the Witch and the Wardrobe',    'Fantasy',         'CHILD', 10.99, '1950-10-16', 'C.S. Lewis',               208,  55, 'Classic fantasy for children',      'Four children discover a magical world through a wardrobe.',                              'ENGLISH');